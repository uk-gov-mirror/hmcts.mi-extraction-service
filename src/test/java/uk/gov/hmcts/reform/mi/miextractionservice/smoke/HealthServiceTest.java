package uk.gov.hmcts.reform.mi.miextractionservice.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.mi.micore.exception.ServiceNotAvailableException;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HealthServiceTest {

    @InjectMocks
    private HealthService classToTest;
    @Mock
    private BlobExportService blobService;

    @Test
    public void testCheckAllDependencies() throws ServiceNotAvailableException {
        classToTest.check();
        verify(blobService, times(1)).checkStorageConnection();
    }

    @Test
    public void testExceptionOnDependencyFail() {
        doThrow(new RuntimeException()).when(blobService).checkStorageConnection();
        assertThrows(ServiceNotAvailableException.class, () -> classToTest.check());
    }
}
