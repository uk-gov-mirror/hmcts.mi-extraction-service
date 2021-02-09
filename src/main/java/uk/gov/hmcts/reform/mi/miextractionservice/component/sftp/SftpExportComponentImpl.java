package uk.gov.hmcts.reform.mi.miextractionservice.component.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.encryption.PgpEncryptionComponentImpl;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;
import uk.gov.hmcts.reform.mi.miextractionservice.util.FileUtils;

import java.time.Duration;

import static java.util.Objects.nonNull;

@Component
@Slf4j
@RequiredArgsConstructor
@Builder
public class SftpExportComponentImpl implements SftpExportComponent {

    private static final String PROTOCOL = "sftp";
    private static final int CONNECTION_TIMEOUT_MILLIS = 60_000;
    private static final String FOLDER_DELIMITER = "/";

    @Value("${sftp.remote.host}")
    private final String remoteHost;
    @Value("${sftp.remote.user}")
    private final String remoteUser;
    @Value("${sftp.remote.password}")
    private final String remotePassword;
    @Value("${sftp.remote.port:22}")
    private final int port;
    @Value("${sftp.remote.folder}")
    private final String destinyFolder;
    @Value("${sftp.enabled:false}")
    private final boolean enabled;
    @Value("${sftp.forceCheck:false}")
    private final boolean forceCheck;
    private final PgpEncryptionComponentImpl pgpEncryptionComponentImpl;
    private final JSch jsch;

    @Override
    public void copyFile(String file) {
        copyFile(file, null);
    }

    @Override
    public void copyFile(String file, String directory) {
        if (enabled) {
            String fileToCopy = getEncryptedFileName(file);
            Session session = null;
            try {
                session = getJshSession();
                setupStpChannel(session);
                ChannelSftp sftpChannel = setupStpChannel(session);
                final String folderName = nonNull(directory)
                    ? destinyFolder.concat(directory).concat(FOLDER_DELIMITER)
                    : destinyFolder;
                checkFolder(sftpChannel, folderName);

                RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                    .handle(SftpException.class)
                    .withDelay(Duration.ofSeconds(2L))
                    .withMaxRetries(3);

                Failsafe.with(retryPolicy).run(
                    () -> sftpChannel.put(fileToCopy, folderName + fileToCopy)
                );
            } catch (JSchException | SftpException | FailsafeException e) {
                throw new ExportException("Unable to send file to sftp server " + fileToCopy, e);
            } finally {
                if (session != null) {
                    session.disconnect();
                }
                FileUtils.deleteFile(fileToCopy);
            }
            log.info("File {} send to sftp server", fileToCopy);
        } else {
            log.info("SFTP component is disabled");
        }
    }

    private String getEncryptedFileName(String fileName) {
        return pgpEncryptionComponentImpl.encryptDataToFile(fileName);
    }

    private void checkFolder(ChannelSftp sftpChannel, String folderName) throws SftpException {
        try {
            sftpChannel.stat(folderName);
        } catch (final SftpException e) {
            // dir does not exist.
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                sftpChannel.mkdir(folderName);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void checkConnection() {
        if (enabled || forceCheck) {
            Session session = null;
            try {
                session = getJshSession();
                setupStpChannel(session);
            } catch (JSchException e) {
                log.warn("Error connecting SFTP server [{}]", remoteHost);
                throw new ExportException("Unable to connect file to sftp server", e);
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
        } else {
            log.info("SFTP component is disabled");
        }
    }

    @Override
    public void loadFile(String file, String destinyFilePath) {
        loadFile(file, null, destinyFilePath);
    }

    @Override
    public void loadFile(String file, String directory, String destinyFilePath) {
        Session session = null;
        try {
            session = getJshSession();
            ChannelSftp sftpChannel = setupStpChannel(session);
            final String folderName = nonNull(directory)
                ? destinyFolder.concat(directory).concat(FOLDER_DELIMITER)
                : destinyFolder;
            sftpChannel.get(folderName + file, destinyFilePath);
        } catch (JSchException | SftpException e) {
            throw new ExportException("Unable to send file to sftp server", e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private ChannelSftp setupStpChannel(Session session) throws JSchException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel(PROTOCOL);
        channelSftp.connect(CONNECTION_TIMEOUT_MILLIS);
        return channelSftp;
    }

    private Session getJshSession() throws JSchException {
        Session jschSession = jsch.getSession(remoteUser, remoteHost, port);
        jschSession.setPassword(remotePassword);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect(CONNECTION_TIMEOUT_MILLIS);
        return jschSession;
    }
}
