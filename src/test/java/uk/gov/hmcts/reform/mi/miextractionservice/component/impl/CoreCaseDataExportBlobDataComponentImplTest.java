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
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobDownloadComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CheckWhitelistComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.EncryptArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.PagedIterableStub;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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

@SuppressWarnings({"PMD.UnusedPrivateField","PMD.ExcessiveImports","PMD.TooManyMethods"})
@ExtendWith(SpringExtension.class)
public class CoreCaseDataExportBlobDataComponentImplTest {

    private static final String OUTPUT_ASSERTION_MATCHING_ERROR = "Returned blob name does not match the expected.";

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String MAX_LINES_FIELD_PROPERTY = "maxLines";

    private static final String TEST_CONTAINER_NAME = "ccd-data-test";
    private static final String TEST_BLOB_NAME_ONE = "data-test-2000-01";
    private static final String TEST_BLOB_NAME_TWO = "data-test-2000-02";

    private static final String TEST_OUTPUT_CONTAINER_NAME = "ccd";
    private static final String TEST_OUTPUT_BLOB_NAME = "1999-12-01-2001-01-01-CCD_EXTRACT.zip";

    private static final String CCD_WORKING_FILE_NAME = "CCD_EXTRACT.csv";
    private static final String CCD_WORKING_ARCHIVE = "CCD_EXTRACT.zip";

    @Mock
    private WriterWrapper writerWrapper;

    @Mock
    private CheckWhitelistComponent checkWhitelistComponent;

    @Mock
    private BlobDownloadComponent blobDownloadComponent;

    @Mock
    private DataParserComponent<CoreCaseData> dataParserComponent;

    @Mock
    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;

    @Mock
    private CsvWriterComponent<OutputCoreCaseData> csvWriterComponent;

    @Mock
    private EncryptArchiveComponent encryptArchiveComponent;

    @Spy
    private ReaderUtil readerUtil;

    @Spy
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private CoreCaseDataExportBlobDataComponentImpl underTest;

    private BlobServiceClient sourceBlobServiceClient;
    private BlobServiceClient targetBlobServiceClient;

    private BufferedWriter bufferedWriter;

    @BeforeEach
    public void setUp() throws IOException {
        sourceBlobServiceClient = mock(BlobServiceClient.class);
        targetBlobServiceClient = mock(BlobServiceClient.class);

        ReflectionTestUtils.setField(underTest, MAX_LINES_FIELD_PROPERTY, "3000");

        bufferedWriter = spy(Files.newBufferedWriter(Paths.get(CCD_WORKING_FILE_NAME)));
        when(writerWrapper.getBufferedWriter(any())).thenReturn(bufferedWriter);
        when(checkWhitelistComponent.isContainerWhitelisted(anyString())).thenReturn(true);
    }

    @Test
    public void givenBlobServiceClientsAndDatesToExtract_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() throws Exception {
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

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(dataInPresentAndFuture.getBytes()));
        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_TWO))
            .thenReturn(new ByteArrayInputStream(TEST_CCD_JSONL_OUTDATED_PAST.getBytes()));

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

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(TEST_OUTPUT_BLOB_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(csvWriterComponent, times(1))
            .writeBeansWithWriter(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
        verify(encryptArchiveComponent)
            .createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(CCD_WORKING_ARCHIVE, true);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenDataOverMaxLines_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        ReflectionTestUtils.setField(underTest, MAX_LINES_FIELD_PROPERTY, "1");

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        String multiLineData = TEST_CCD_JSONL + "\n" + TEST_CCD_JSONL + "\n" + TEST_CCD_JSONL;

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(multiLineData.getBytes()));

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(TEST_OUTPUT_BLOB_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(TEST_OUTPUT_BLOB_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(csvWriterComponent, times(3))
            .writeBeansWithWriter(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
        verify(encryptArchiveComponent)
            .createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(CCD_WORKING_ARCHIVE, true);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenNoDataWithLatestDate_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);
        BlobItem blobItemTwo = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne, blobItemTwo));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);
        when(blobItemTwo.getName()).thenReturn(TEST_BLOB_NAME_TWO);

        String dataInPresentAndFuture = TEST_CCD_JSONL + "\n" + TEST_CCD_JSONL_OUTDATED_FUTURE;

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(dataInPresentAndFuture.getBytes()));
        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_TWO))
            .thenReturn(new ByteArrayInputStream("".getBytes()));

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_FUTURE))
            .thenReturn(TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(TEST_OUTPUT_BLOB_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(TEST_OUTPUT_BLOB_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(csvWriterComponent)
            .writeBeansWithWriter(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
        verify(encryptArchiveComponent)
            .createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(CCD_WORKING_ARCHIVE, true);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenDatesToExtractWithNoExistingContainer_whenExportBlobDataAndGetUrl_thenCreateContainerAndReturnUrlOfUploadedExtractedDataBlob()
        throws Exception {

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(TEST_CCD_JSONL.getBytes()));

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(TEST_OUTPUT_BLOB_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(TEST_OUTPUT_BLOB_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, times(1)).create();
        verify(csvWriterComponent)
            .writeBeansWithWriter(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
        verify(encryptArchiveComponent)
            .createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(CCD_WORKING_ARCHIVE, true);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenSameFromDate_whenExportBlobDataAndGetUrl_thenCreateContainerAndReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        OffsetDateTime fromDateSameAsEventDate = OffsetDateTime.of(2000, 01, 29, 0, 0, 0, 0, ZoneOffset.UTC);
        String testOutputBlobName = "2000-01-29-2001-01-01-CCD_EXTRACT.zip";

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(TEST_CCD_JSONL.getBytes()));

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(testOutputBlobName)).thenReturn(targetBlobClient);

        String result = underTest
            .exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, fromDateSameAsEventDate, TEST_TO_DATE_TIME);

        assertEquals(testOutputBlobName, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, times(1)).create();
        verify(csvWriterComponent)
            .writeBeansWithWriter(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
        verify(encryptArchiveComponent)
            .createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(CCD_WORKING_ARCHIVE, true);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenSameToDate_whenExportBlobDataAndGetUrl_thenCreateContainerAndReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        OffsetDateTime toDateSameAsEventDate = OffsetDateTime.of(2000, 01, 29, 0, 0, 0, 0, ZoneOffset.UTC);
        String testOutputBlobName = "1999-12-01-2000-01-29-CCD_EXTRACT.zip";

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(TEST_CCD_JSONL.getBytes()));

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(testOutputBlobName)).thenReturn(targetBlobClient);

        String result = underTest
            .exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, toDateSameAsEventDate);

        assertEquals(testOutputBlobName, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, times(1)).create();
        verify(csvWriterComponent)
            .writeBeansWithWriter(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
        verify(encryptArchiveComponent)
            .createEncryptedArchive(Collections.singletonList(CCD_WORKING_FILE_NAME), CCD_WORKING_ARCHIVE);
        verify(targetBlobClient).uploadFromFile(CCD_WORKING_ARCHIVE, true);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenNoWhitelistedContainers_whenExportData_thenReturnNullForBlobName() {
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        when(checkWhitelistComponent.isContainerWhitelisted(TEST_CONTAINER_NAME)).thenReturn(false);

        assertEquals(
            null,
            underTest.exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Expected null output when no containers match whitelist.");
    }

    @Test
    public void givenNoDataToOutput_whenExportBlobData_thenReturnNullForBlobName() {
        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>());

        assertEquals(
            null,
            underTest.exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Expected null output when no containers exist.");
    }

    @Test
    public void givenNoDataAtAllInRetrievedBlobs_whenExportBlobDataAndGetUrl_thenReturnNullForBlobName() throws Exception {
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(" ".getBytes()));

        assertEquals(
            null,
            underTest.exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Expected null output when no query matching data found.");

        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenBufferedWriterWithException_whenExportBlobData_thenVerifyWriterIsClosed() throws Exception {
        doThrow(new IOException("Broken write.")).when(bufferedWriter).write(anyString());

        doAnswer((Answer<Void>) invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("Should throw IOException");
            return null;
        }).when(csvWriterComponent).writeHeadersToCsvFile(any());

        assertThrows(ParserException.class, () ->
            underTest.exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME));

        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenExceptionWhenReadingInputStream_whenExportBlobDataAndGetUrl_throwParserException()
        throws Exception {

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        try (InputStream inputStream = mock(InputStream.class)) {
            when(inputStream.read()).thenThrow(new IOException("Exception on read."));

            when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
                .thenReturn(inputStream);

            assertThrows(ParserException.class, () ->
                underTest.exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME));

            // One close each for Buffered Reader and InputStreamReader in try-with-resource
            verify(inputStream, times(2)).close();
            verify(bufferedWriter, times(1)).close();
        }
    }
}