package uk.gov.hmcts.reform.mi.miextractionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Clock;

@SuppressWarnings({"PMD.CloseResource","PMD.AvoidUsingHardCodedIP"})
@Configuration
@ComponentScan(basePackages = "uk.gov.hmcts.reform",
    excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationRunner.class) })
public class TestConfig {

    public static int mailerPort;

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JavaMailSenderImpl jvaMailSender() throws IOException {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost("127.0.0.1");

        ServerSocket serverSocket = new ServerSocket(0);
        mailerPort = serverSocket.getLocalPort();
        // Close the ServerSocket running on the port so mailer service can be started on it.
        serverSocket.close();

        javaMailSender.setPort(mailerPort);

        return javaMailSender;
    }
}
