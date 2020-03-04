package uk.gov.hmcts.reform.mi.miextractionservice.service.impl;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SasIpWhitelist;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.PagedIterableStub;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.CCD_OUTPUT_CONTAINER_NAME;

@ExtendWith(SpringExtension.class)
public class BlobExportServiceImplTest {

    private static final String TEST_BLOB_NAME = "testBlobName";
    private static final String TEST_BLOB_URL = "testBlobUrl";
    private static final OffsetDateTime FIXED_DATETIME = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private SasIpWhitelist sasIpWhitelist;

    @Mock
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Mock
    private ExportBlobDataComponent exportBlobDataComponent;

    @Mock
    private GenerateBlobUrlComponent generateBlobUrlComponent;

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
        when(sasIpWhitelist.getRange()).thenReturn(Collections.emptyMap());

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(7L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L))
        )).thenReturn(TEST_BLOB_NAME);

        when(generateBlobUrlComponent.generateUrlForBlob(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    public void givenDates_whenExportData_thenSendEmailWithBlobUrlForDates() {
        ReflectionTestUtils.setField(underTest, "retrieveFromDate", "2000-01-10");
        ReflectionTestUtils.setField(underTest, "retrieveToDate", "2000-01-20");

        when(sasIpWhitelist.getRange()).thenReturn(Collections.emptyMap());

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC)),
            eq(OffsetDateTime.of(2000, 1, 20, 23, 59, 59, 999, ZoneOffset.UTC))
        )).thenReturn(TEST_BLOB_NAME);

        when(generateBlobUrlComponent.generateUrlForBlob(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME)).thenReturn(TEST_BLOB_URL);

        underTest.exportBlobs();

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(TEST_BLOB_URL);
    }

    @Test
    public void givenListOfWhitelistedIps_whenExportData_thenSendEmailWitMultipleBlobUrls() {
        String homeOffice = "Home-Office";
        String homeIp = "homeIp";
        String workOffice = "Work-Office";
        String workIp = "workIp";

        when(sasIpWhitelist.getRange()).thenReturn(ImmutableMap.of(
            homeOffice, homeIp,
            workOffice, workIp
        ));

        when(exportBlobDataComponent.exportBlobsAndGetOutputName(
            eq(stagingClient),
            eq(exportClient),
            eq(FIXED_DATETIME.minusDays(7L)),
            eq(FIXED_DATETIME.minusDays(1L).plusHours(23L).plusMinutes(59L).plusSeconds(59L).plusNanos(999L))
        )).thenReturn(TEST_BLOB_NAME);

        String workBlobUrl = "testWorkBlob";

        when(generateBlobUrlComponent.generateUrlForBlobWithIpRange(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME, homeIp))
            .thenReturn(TEST_BLOB_URL);
        when(generateBlobUrlComponent.generateUrlForBlobWithIpRange(exportClient, CCD_OUTPUT_CONTAINER_NAME, TEST_BLOB_NAME, workIp))
            .thenReturn(workBlobUrl);

        underTest.exportBlobs();

        String expectedMessage = homeOffice.replaceAll("-", " ") + " : " + TEST_BLOB_URL
            + "\n\n"
            + workOffice.replaceAll("-", " ") + " : " + workBlobUrl
            + "\n\n";

        verify(emailBlobUrlToTargetsComponent).sendBlobUrl(expectedMessage);
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
