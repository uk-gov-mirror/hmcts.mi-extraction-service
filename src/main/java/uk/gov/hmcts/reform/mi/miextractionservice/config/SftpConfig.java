package uk.gov.hmcts.reform.mi.miextractionservice.config;

import com.jcraft.jsch.JSch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SftpConfig {

    @Bean
    public JSch jsch() {
        return new JSch();
    }

}
