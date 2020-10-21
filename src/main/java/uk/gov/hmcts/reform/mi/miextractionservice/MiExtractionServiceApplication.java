package uk.gov.hmcts.reform.mi.miextractionservice;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;
import uk.gov.hmcts.reform.mi.miextractionservice.service.export.ExportService;

@Slf4j
@AllArgsConstructor
@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform")
public class MiExtractionServiceApplication implements ApplicationRunner {

    private final @Value("${smoke.test.enabled:false}") boolean smokeTest;
    private final @Value("${telemetry.wait.period:10000}") int waitPeriod;
    private final ExportService exportService;
    private final HealthCheck healthCheck;
    private final TelemetryClient client;

    public static void main(final String[] args) {
        SpringApplication.run(MiExtractionServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            if (smokeTest) {
                healthCheck.check();
            } else {
                runSilentCheck();
                log.info("Starting application runner.");
                exportService.exportData();
                log.info("Finished application runner.");
            }
        } catch (Exception e) {
            log.error("Error executing extraction service", e);
            throw e;
        } finally {
            client.flush();
            waitTelemetryGracefulPeriod();
        }
    }

    private void runSilentCheck() {
        try {
            healthCheck.check();
        } catch (Exception e) {
            log.warn("Health check failed");
        }
    }

    private void waitTelemetryGracefulPeriod() throws InterruptedException {
        Thread.sleep(waitPeriod);
    }
}
