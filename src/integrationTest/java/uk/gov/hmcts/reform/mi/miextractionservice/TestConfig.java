package uk.gov.hmcts.reform.mi.miextractionservice;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.mi.micore", "uk.gov.hmcts.reform.mi.miextractionservice.test"})
public class TestConfig {
    // Spring Test configuration class.
}
