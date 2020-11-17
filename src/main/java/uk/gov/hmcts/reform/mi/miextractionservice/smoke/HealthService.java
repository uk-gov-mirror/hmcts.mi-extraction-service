package uk.gov.hmcts.reform.mi.miextractionservice.smoke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;
import uk.gov.hmcts.reform.mi.micore.exception.ServiceNotAvailableException;
import uk.gov.hmcts.reform.mi.miextractionservice.component.sftp.SftpExportComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.service.export.ExportService;

@Component
@Slf4j
@RequiredArgsConstructor
public class HealthService implements HealthCheck {

    private final ExportService exportService;

    private final SftpExportComponent sftpExportComponent;

    @Override
    public void check() throws ServiceNotAvailableException {
        try {
            exportService.checkStorageConnection();
            sftpExportComponent.checkConnection();
            log.info("Health check completed");
        } catch (Exception e) {
            throw new ServiceNotAvailableException("Not able to connect to dependency", e);
        }
    }
}
