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

import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobMessageBuilderComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;
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
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_OUTPUT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_WORKING_FILE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_OUTPUT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_WORKING_FILE_NAME;

@ExtendWith(SpringExtension.class)
class BlobExportServiceImplTest {

    private static final String DATA_SOURCE_FILTER_KEY = "dataSource";
    private static final String TEST_BLOB_NAME = "testBlobName";
    private static final String TEST_BLOB_URL = "testBlobUrl";
    private static final OffsetDateTime FIXED_DATETIME = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Mock
    private ExportBlobDataComponent exportBlobDataComponent;

    @Mock
    private BlobMessageBuilderComponent blobMessageBuilderComponent;

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
    void setUp() {
        ReflectionTestUtils.setField(underTest, DATA_SOURCE_FILTER_KEY, "all");

        stagingClient = mock(BlobServiceClient.class);
        exportClient = mock(BlobServiceClient.class);

        when(extractionBlobServiceClientFactory.getStagingClient()).thenReturn(stagingClient);
        when(extractionBlobServiceClientFactory.getExportClient()).thenReturn(exportClient);

        when(dateTimeUtil.getCurrentDateTime()).thenReturn(FIXED_DATETIME);
        when(dateTimeUtil.parseDateString(anyString())).thenCallRealMethod();
    }

    @Test
    void givenNoDates_whenExportAllData_thenSendEmailWithBlobUrlForDailyData() {
        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(1L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L)),
            eq(CCD_WORKING_FILE_NAME),
            eq(SourceEnum.CORE_CASE_DATA)
        )).thenReturn(TEST_BLOB_NAME);

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(1L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L)),
            eq(NOTIFY_WORKING_FILE_NAME),
            eq(SourceEnum.NOTIFY)
        )).thenReturn(TEST_BLOB_NAME);

        when(blobMessageBuilderComponent.buildMessage(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);
        when(blobMessageBuilderComponent.buildMessage(exportClient, NOTIFY_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL + "\n" + TEST_BLOB_URL);
    }

    @Test
    void givenNoDates_whenExportCcdData_thenSendEmailWithBlobUrlForDailyData() {
        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(1L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L)),
            eq(CCD_WORKING_FILE_NAME),
            eq(SourceEnum.CORE_CASE_DATA)
        )).thenReturn(TEST_BLOB_NAME);

        when(blobMessageBuilderComponent.buildMessage(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(exportBlobDataComponent).exportBlobsAndGetOutputName(any(), any(), any(), any(), any(), eq(SourceEnum.NOTIFY));
        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    void givenNoDates_whenExportNotifyData_thenSendEmailWithBlobUrlForDailyData() {
        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(1L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L)),
            eq(NOTIFY_WORKING_FILE_NAME),
            eq(SourceEnum.NOTIFY)
        )).thenReturn(TEST_BLOB_NAME);

        when(blobMessageBuilderComponent.buildMessage(exportClient, NOTIFY_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(exportBlobDataComponent).exportBlobsAndGetOutputName(any(), any(), any(), any(), any(), eq(SourceEnum.CORE_CASE_DATA));
        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    void givenDataSourceFilterToCcd_whenExportCcdData_thenSendEmailWithBlobUrlForDailyData() {
        ReflectionTestUtils.setField(underTest, DATA_SOURCE_FILTER_KEY, "CoreCaseData");

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(1L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L)),
            eq(CCD_WORKING_FILE_NAME),
            eq(SourceEnum.CORE_CASE_DATA)
        )).thenReturn(TEST_BLOB_NAME);

        when(blobMessageBuilderComponent.buildMessage(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(exportBlobDataComponent, never()).exportBlobsAndGetOutputName(any(), any(), any(), any(), any(), eq(SourceEnum.NOTIFY));
        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    void givenDataSourceFilterToNotify_whenExportNotifyData_thenSendEmailWithBlobUrlForDailyData() {
        ReflectionTestUtils.setField(underTest, DATA_SOURCE_FILTER_KEY, "Notify");

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(1L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L)),
            eq(NOTIFY_WORKING_FILE_NAME),
            eq(SourceEnum.NOTIFY)
        )).thenReturn(TEST_BLOB_NAME);

        when(blobMessageBuilderComponent.buildMessage(exportClient, NOTIFY_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(exportBlobDataComponent, never()).exportBlobsAndGetOutputName(any(), any(), any(), any(), any(), eq(SourceEnum.CORE_CASE_DATA));
        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    void givenDates_whenExportData_thenSendEmailWithBlobUrlForDates() {
        ReflectionTestUtils.setField(underTest, "retrieveFromDate", "2000-01-10");
        ReflectionTestUtils.setField(underTest, "retrieveToDate", "2000-01-20");

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC)),
            eq(OffsetDateTime.of(2000, 1, 20, 23, 59, 59, 999, ZoneOffset.UTC)),
            eq(CCD_WORKING_FILE_NAME),
            eq(SourceEnum.CORE_CASE_DATA)
        )).thenReturn(TEST_BLOB_NAME);

        when(blobMessageBuilderComponent.buildMessage(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    void givenNoOutputBlob_whenExportData_thenDoNotSendEmail() {
        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(null);

        underTest.exportBlobs();

        verify(blobMessageBuilderComponent, never()).buildMessage(any(), any(), any());
        verify(emailBlobUrlToTargetsComponent, never()).sendBlobUrl(any());
    }

    @Test
    void testAllStorageAccountsConnection() {
        when(stagingClient.listBlobContainers())
            .thenReturn(new PagedIterableStub<>(blobContainerItem));
        when(exportClient.listBlobContainers())
            .thenReturn(new PagedIterableStub<>(blobContainerItem));

        underTest.checkStorageConnection();
        verify(stagingClient, times(1)).listBlobContainers();
        verify(exportClient, times(1)).listBlobContainers();
    }
}
