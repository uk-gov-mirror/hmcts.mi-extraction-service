package uk.gov.hmcts.reform.mi.miextractionservice.service.impl;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.PagedIterableStub;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class BlobExportServiceImplTest {

    private static final String TEST_BLOB_NAME = "testBlobName";
    private static final OffsetDateTime FIXED_DATETIME = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Mock
    private ExportBlobDataComponent exportBlobDataComponent;

    @Mock
    private EmailBlobUrlToTargetsComponent emailBlobUrlToTargetsComponent;

    @Mock
    private DateTimeUtil dateTimeUtil;

    @Mock
    private BlobContainerItem blobContainerItem;

    @InjectMocks
    private BlobExportServiceImpl underTest;

    private BlobServiceClient stagingClient;
    private BlobServiceClient exportClient;

    @BeforeEach
    public void setUp() {
        stagingClient = mock(BlobServiceClient.class);
        exportClient = mock(BlobServiceClient.class);

        when(extractionBlobServiceClientFactory.getStagingClient()).thenReturn(stagingClient);
        when(extractionBlobServiceClientFactory.getExportClient()).thenReturn(exportClient);

        when(dateTimeUtil.getCurrentDateTime()).thenReturn(FIXED_DATETIME);
        when(dateTimeUtil.parseDateString(anyString())).thenCallRealMethod();
    }

    @Test
    public void givenNoDates_whenExportData_thenSendEmailWithBlobUrlForWeeklyData() {
        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(7L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L))
        )).thenReturn(TEST_BLOB_NAME);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_NAME);
    }

    @Test
    public void givenDates_whenExportData_thenSendEmailWithBlobUrlForDates() {
        ReflectionTestUtils.setField(underTest, "retrieveFromDate", "2000-01-10");
        ReflectionTestUtils.setField(underTest, "retrieveToDate", "2000-01-20");

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC)),
            eq(OffsetDateTime.of(2000, 1, 20, 23, 59, 59, 999, ZoneOffset.UTC))
        )).thenReturn(TEST_BLOB_NAME);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_NAME);
    }

    @Test
    public void givenNoOutputBlob_whenExportData_thenDoNotSendEmail() {
        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(null);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent, never()).sendBlobUrl(any());
    }

    @Test
    public void testAllStorageAccountsConnection() {
        when(stagingClient.listBlobContainers())
            .thenReturn(new PagedIterableStub<>(blobContainerItem));
        when(exportClient.listBlobContainers())
            .thenReturn(new PagedIterableStub<>(blobContainerItem));

        underTest.checkStorageConnection();
        verify(stagingClient, times(1)).listBlobContainers();
        verify(exportClient, times(1)).listBlobContainers();
    }
}
