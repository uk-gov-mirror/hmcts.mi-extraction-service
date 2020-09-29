package uk.gov.hmcts.reform.mi.miextractionservice.test.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Slf4j
@ConditionalOnProperty(name = "sftp.enabled",havingValue = "true")
public class SftpClient {

    private static final String PROTOCOL = "sftp";
    private static final int CONNECTION_TIMEOUT_MILLIS = 60_000;

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

    private Session session;
    private ChannelSftp sftpChannel;

    @PostConstruct
    public void init() throws JSchException {
        session = getJshSession();
        sftpChannel = setupStpChannel(session);
        log.info("Sftp client initialised");
    }

    @PreDestroy
    public void destroy() {
        if (session != null) {
            session.disconnect();
        }
        log.info("Sftp client disconnected");
    }

    public void loadFile(String file, String destinyFilePath) throws SftpException {
        sftpChannel.get(destinyFolder + file, destinyFilePath);
    }

    public void deleteFile(String file) throws SftpException {
        sftpChannel.rm(destinyFolder + file);
    }

    private ChannelSftp setupStpChannel(Session session) throws JSchException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel(PROTOCOL);
        channelSftp.connect(CONNECTION_TIMEOUT_MILLIS);
        return channelSftp;
    }

    private Session getJshSession() throws JSchException {
        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(remoteUser, remoteHost, port);
        jschSession.setPassword(remotePassword);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.connect(CONNECTION_TIMEOUT_MILLIS);
        return jschSession;
    }
}
