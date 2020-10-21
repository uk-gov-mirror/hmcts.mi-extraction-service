package uk.gov.hmcts.reform.mi.miextractionservice.component.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

@Component
@Slf4j
public class SftpExportComponentImpl implements SftpExportComponent {

    private static final String PROTOCOL = "sftp";
    private static final int CONNECTION_TIMEOUT_MILLIS =  60_000;

    @Value("${sftp.remote.host}")
    private String remoteHost;
    @Value("${sftp.remote.user}")
    private String remoteUser;
    @Value("${sftp.remote.password}")
    private String remotePassword;
    @Value("${sftp.remote.port:22}")
    private int port;
    @Value("${sftp.remote.folder}")
    private String destinyFolder;
    @Value("${sftp.enabled:false}")
    private boolean enabled;
    @Value("${sftp.forceCheck:false}")
    private boolean forceCheck;

    @Autowired
    private JSch jsch;

    @Override
    public void copyFile(String file) {
        if (enabled) {
            Session session = null;
            try {
                session = getJshSession();
                setupStpChannel(session);
                ChannelSftp sftpChannel = setupStpChannel(session);
                checkFolder(sftpChannel);
                sftpChannel.put(file, destinyFolder + file);
            } catch (JSchException | SftpException e) {
                throw new ExportException("Unable to send file to sftp server", e);
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
            log.info("File {} send to sftp server", file);
        } else {
            log.info("SFTP component is disabled");
        }
    }

    private void checkFolder(ChannelSftp sftpChannel) throws SftpException {
        try {
            sftpChannel.stat(destinyFolder);
        } catch (final SftpException e) {
            // dir does not exist.
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                sftpChannel.mkdir(destinyFolder);
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
        Session session = null;
        try {
            session = getJshSession();
            ChannelSftp sftpChannel = setupStpChannel(session);
            sftpChannel.get(destinyFolder + file, destinyFilePath);
        } catch (JSchException | SftpException e) {
            throw new ExportException("Unable to send file to sftp server", e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private ChannelSftp setupStpChannel(Session session) throws JSchException {
        ChannelSftp channelSftp =  (ChannelSftp) session.openChannel(PROTOCOL);
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
