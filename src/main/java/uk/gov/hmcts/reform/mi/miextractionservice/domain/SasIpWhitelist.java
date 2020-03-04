package uk.gov.hmcts.reform.mi.miextractionservice.domain;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConfigurationProperties(prefix = "sas-ip-whitelist")
@Getter
public class SasIpWhitelist {

    private Map<String, String> range = new ConcurrentHashMap<>();
}
