package uk.gov.hmcts.reform.mi.miextractionservice;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;
import uk.gov.hmcts.reform.mi.micore.exception.ServiceNotAvailableException;
import uk.gov.hmcts.reform.mi.miextractionservice.service.export.ExportService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MiExtractionServiceApplicationTest {

    private static final int TEST_WAIT_PERIOD = 1000;

    @Mock private ExportService exportService;
    @Mock private HealthCheck healthCheck;
    @Mock private TelemetryClient client;

    private MiExtractionServiceApplication classToTest;

    @Test
    void testApplicationExecuted() throws Exception {
        classToTest = new MiExtractionServiceApplication(false, TEST_WAIT_PERIOD, exportService, healthCheck, client);

        classToTest.run(null);

        verify(exportService, times(1)).exportData();
        verify(healthCheck, never()).check();
        verify(client, times(1)).flush();
    }

    @Test
    void testSmokeCheckExecuted() throws Exception {
        classToTest = new MiExtractionServiceApplication(true, TEST_WAIT_PERIOD, exportService, healthCheck, client);

        classToTest.run(null);

        verify(healthCheck, times(1)).check();
        verify(exportService, never()).exportData();
        verify(client, times(1)).flush();
    }

    @Test
    void testSmokeCheckExceptionPropagated() throws Exception {
        classToTest = new MiExtractionServiceApplication(true, TEST_WAIT_PERIOD, exportService, healthCheck, client);
        doThrow(new ServiceNotAvailableException("Not available")).when(healthCheck).check();

        assertThrows(ServiceNotAvailableException.class, () -> classToTest.run(null),
                     "Should throw ServiceNotAvailableException when health check fails.");

        verify(healthCheck, times(1)).check();
        verify(exportService, never()).exportData();
        verify(client, times(1)).flush();
    }
}
