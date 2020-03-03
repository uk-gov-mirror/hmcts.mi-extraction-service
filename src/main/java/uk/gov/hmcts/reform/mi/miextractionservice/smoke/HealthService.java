package uk.gov.hmcts.reform.mi.miextractionservice.smoke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;
import uk.gov.hmcts.reform.mi.micore.exception.ServiceNotAvailableException;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;

@Component
@Slf4j
@RequiredArgsConstructor
public class HealthService implements HealthCheck {

    @Autowired
    private final BlobExportService blobService;

    @Override
    public void check() throws ServiceNotAvailableException {
        try {
            blobService.checkStorageConnection();
            log.info("Health check completed");
        } catch (Exception e) {
            throw new ServiceNotAvailableException("Not able to connect to dependency", e);
        }
    }
}
