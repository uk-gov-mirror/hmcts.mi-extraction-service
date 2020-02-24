package uk.gov.hmcts.reform.mi.miextractionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;

import java.time.Clock;

@Slf4j
@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform")
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class MiExtractionServiceApplication implements ApplicationRunner {

    @Autowired
    private BlobExportService blobExportService;
    @Autowired
    private HealthCheck healthCheck;
    @Value("${smoke.test.enabled:false}")
    private boolean smokeTest;

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public static void main(final String[] args) {
        SpringApplication.run(MiExtractionServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (smokeTest) {
            healthCheck.check();
        } else {
            log.info("Starting application runner.");

            blobExportService.exportBlobs();

            log.info("Finished application runner.");
        }
    }
}
