package uk.gov.hmcts.reform.mi.miextractionservice.smoke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.mi.micore.exception.ServiceNotAvailableException;
import uk.gov.hmcts.reform.mi.miextractionservice.component.sftp.SftpExportComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.service.export.ExportService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private ExportService exportService;

    @Mock
    private SftpExportComponent sftpExportComponent;

    private HealthService classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new HealthService(exportService, sftpExportComponent);
    }

    @Test
    void testCheckAllDependencies() throws ServiceNotAvailableException {
        classToTest.check();

        verify(exportService, times(1)).checkStorageConnection();
        verify(sftpExportComponent, times(1)).checkConnection();
    }

    @Test
    void testExceptionOnDependencyFail() {
        doThrow(new RuntimeException()).when(exportService).checkStorageConnection();

        assertThrows(ServiceNotAvailableException.class, () -> classToTest.check(),
                     "Should throw ServiceNotAvailableException when health check fails.");
    }

    @Test
    void testExceptionOnSftpComponentFail() {
        doThrow(new RuntimeException()).when(sftpExportComponent).checkConnection();
        assertThrows(ServiceNotAvailableException.class, () -> classToTest.check());
    }
}
