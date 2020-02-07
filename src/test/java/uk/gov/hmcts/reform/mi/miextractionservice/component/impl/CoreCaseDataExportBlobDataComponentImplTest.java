package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.component.BlobDownloadComponent;
import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.EncryptArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;
import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.PagedIterableStub;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_AS_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_FUTURE;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_PAST;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA;

@SuppressWarnings({"PMD.UnusedPrivateField","PMD.ExcessiveImports"})
@ExtendWith(SpringExtension.class)
public class CoreCaseDataExportBlobDataComponentImplTest {

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String TEST_CONTAINER_NAME = "ccd-data-test";
    private static final String TEST_BLOB_NAME_ONE = "data-test-2000-01";
    private static final String TEST_BLOB_NAME_TWO = "data-test-2000-02";

    private static final String TEST_OUTPUT_CONTAINER_NAME = "ccd";
    private static final String TEST_OUTPUT_BLOB_NAME = "1999-12-01-2001-01-01-CCD_EXTRACT.zip";

    private static final String WORKING_FILE_NAME = "CCD_EXTRACT.csv";
    private static final String WORKING_ARCHIVE = "CCD_EXTRACT.zip";

    private static final String TEST_SAS_URL = "testSasUrl";

    @Mock
    private BlobDownloadComponent<byte[]> blobDownloadComponent;

    @Mock
    private DataParserComponent<CoreCaseData> dataParserComponent;

    @Mock
    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;

    @Mock
    private CsvWriterComponent<OutputCoreCaseData> csvWriterComponent;

    @Mock
    private EncryptArchiveComponent encryptArchiveComponent;

    @Mock
    private GenerateBlobUrlComponent generateBlobUrlComponent;

    @Spy
    private ReaderUtil readerUtil;

    @Spy
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private CoreCaseDataExportBlobDataComponentImpl underTest;

    private BlobServiceClient sourceBlobServiceClient;
    private BlobServiceClient targetBlobServiceClient;

    @BeforeEach
    public void setUp() {
        sourceBlobServiceClient = mock(BlobServiceClient.class);
        targetBlobServiceClient = mock(BlobServiceClient.class);
    }

    @Test
    public void givenBlobServiceClientsAndDatesToExtract_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() {
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        BlobContainerItem notCcdBlobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem, notCcdBlobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);
        when(notCcdBlobContainerItem.getName()).thenReturn("not-data-test");

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);
        BlobItem blobItemTwo = mock(BlobItem.class);
        BlobItem outOfDateBlobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne, blobItemTwo, outOfDateBlobItem));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);
        when(blobItemTwo.getName()).thenReturn(TEST_BLOB_NAME_TWO);
        when(outOfDateBlobItem.getName()).thenReturn("not-data-test-2010-01");

        String dataInPresentAndFuture = TEST_CCD_JSONL + "\n" + TEST_CCD_JSONL_OUTDATED_FUTURE;

        when(blobDownloadComponent.downloadBlob(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(dataInPresentAndFuture.getBytes());
        when(blobDownloadComponent.downloadBlob(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_TWO))
            .thenReturn(TEST_CCD_JSONL_OUTDATED_PAST.getBytes());

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_FUTURE))
            .thenReturn(TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_PAST))
            .thenReturn(TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(TEST_OUTPUT_BLOB_NAME)).thenReturn(targetBlobClient);

        when(generateBlobUrlComponent.generateUrlForBlob(targetBlobServiceClient, TEST_OUTPUT_CONTAINER_NAME, TEST_OUTPUT_BLOB_NAME))
            .thenReturn(TEST_SAS_URL);

        String result = underTest.exportBlobsAndReturnUrl(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME);

        assertEquals(TEST_SAS_URL, result, "Returned url does not match expected.");

        verify(targetBlobContainerClient, never()).create();
        verify(csvWriterComponent).writeBeansAsCsvFile(WORKING_FILE_NAME, Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA));
        verify(encryptArchiveComponent).createEncryptedArchive(Collections.singletonList(WORKING_FILE_NAME), WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(WORKING_ARCHIVE, true);
    }

    @Test
    public void givenDatesToExtractWithNoExistingContainer_whenExportBlobDataAndGetUrl_thenCreateContainerAndReturnUrlOfUploadedExtractedDataBlob() {
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.downloadBlob(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(TEST_CCD_JSONL.getBytes());

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(TEST_OUTPUT_BLOB_NAME)).thenReturn(targetBlobClient);

        when(generateBlobUrlComponent.generateUrlForBlob(targetBlobServiceClient, TEST_OUTPUT_CONTAINER_NAME, TEST_OUTPUT_BLOB_NAME))
            .thenReturn(TEST_SAS_URL);

        String result = underTest.exportBlobsAndReturnUrl(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME);

        assertEquals(TEST_SAS_URL, result, "Returned url does not match the expected url.");

        verify(targetBlobContainerClient, times(1)).create();
        verify(csvWriterComponent).writeBeansAsCsvFile(WORKING_FILE_NAME, Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA));
        verify(encryptArchiveComponent).createEncryptedArchive(Collections.singletonList(WORKING_FILE_NAME), WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(WORKING_ARCHIVE, true);
    }

    @Test
    public void givenNoDataToOutput_whenExportBlobData_thenThrowExportException() {
        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>());

        assertThrows(ExportException.class,
            () -> underTest.exportBlobsAndReturnUrl(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME));
    }
}