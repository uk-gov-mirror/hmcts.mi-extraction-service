package uk.gov.hmcts.reform.mi.miextractionservice.smoke;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;
import uk.gov.hmcts.reform.mi.micore.exception.ServiceNotAvailableException;
import uk.gov.hmcts.reform.mi.miextractionservice.service.export.ExportService;

@Slf4j
@AllArgsConstructor
@Component
public class HealthService implements HealthCheck {

    private final ExportService exportService;

    @Override
    public void check() throws ServiceNotAvailableException {
        try {
            exportService.checkStorageConnection();
            log.info("Health check completed");
        } catch (Exception e) {
            throw new ServiceNotAvailableException("Not able to connect to dependency", e);
        }
    }
}
