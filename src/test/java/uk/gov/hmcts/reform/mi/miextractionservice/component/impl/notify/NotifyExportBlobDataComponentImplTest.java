package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

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

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobDownloadComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CheckWhitelistComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.JsonlWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.MetadataFilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.PagedIterableStub;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.FileWrapper;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@SuppressWarnings({"PMD.UnusedPrivateField","PMD.ExcessiveImports","PMD.TooManyMethods"})
@ExtendWith(SpringExtension.class)
class NotifyExportBlobDataComponentImplTest {

    private static final String OUTPUT_ASSERTION_MATCHING_ERROR = "Returned blob name does not match the expected.";

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String MAX_LINES_FIELD_PROPERTY = "maxLines";
    private static final String ARCHIVE_FLAG_PROPERTY = "archiveFlag";

    private static final String TEST_CONTAINER_NAME = "notify-data-test";
    private static final String TEST_BLOB_NAME_ONE = "data-test-2000-01";
    private static final String TEST_BLOB_NAME_TWO = "data-test-2000-02";

    private static final String TEST_OUTPUT_CONTAINER_NAME = "notify";

    private static final String TEST_DATE_PREFIX = "1999-12-01-2001-01-01-";

    private static final String NOTIFY_WORKING_FILE_NAME = "NOTIFY_EXTRACT.jsonl";
    private static final String NOTIFY_WORKING_ARCHIVE = "NOTIFY_EXTRACT.zip";

    private static final String OUTPUT_NAME = TEST_DATE_PREFIX + NOTIFY_WORKING_FILE_NAME;

    private static final String NOTIFY_JSON = "{\"created_at\":\"2000-01-01T10:00:00.000000Z\"}";
    private static final String NOTIFY_JSON_FUTURE = "{\"created_at\":\"2002-01-01T10:00:00.000000Z\"}";
    private static final String NOTIFY_JSON_PAST = "{\"created_at\":\"1998-01-01T10:00:00.000000Z\"}";

    private static final NotificationOutput NOTIFY_OUTPUT = NotificationOutput.builder().createdAt("2000-01-01T10:00:00.000000Z").build();

    @Mock
    private WriterWrapper writerWrapper;

    @Mock
    private FileWrapper fileWrapper;

    @Mock
    private CheckWhitelistComponent checkWhitelistComponent;

    @Mock
    private MetadataFilterComponent metadataFilterComponent;

    @Mock
    private BlobDownloadComponent blobDownloadComponent;

    @Mock
    private FilterComponent<NotificationOutput> filterComponent;

    @Mock
    private JsonlWriterComponent<NotificationOutput> jsonlWriterComponent;

    @Mock
    private ArchiveComponent archiveComponent;

    @Spy
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private NotifyExportBlobDataComponentImpl underTest;

    private BlobServiceClient sourceBlobServiceClient;
    private BlobServiceClient targetBlobServiceClient;

    private BufferedWriter bufferedWriter;

    @BeforeEach
    void setUp() throws IOException {
        sourceBlobServiceClient = mock(BlobServiceClient.class);
        targetBlobServiceClient = mock(BlobServiceClient.class);

        ReflectionTestUtils.setField(underTest, MAX_LINES_FIELD_PROPERTY, "3000");
        ReflectionTestUtils.setField(underTest, ARCHIVE_FLAG_PROPERTY, "false");

        bufferedWriter = spy(Files.newBufferedWriter(Paths.get(NOTIFY_WORKING_FILE_NAME)));
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenReturn(bufferedWriter);
        when(checkWhitelistComponent.isContainerWhitelisted(anyString())).thenReturn(true);
        when(metadataFilterComponent.filterByMetadata(anyMap())).thenReturn(true);
    }

    @Test
    void givenBlobServiceClientsAndDatesToExtract_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() throws Exception {
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

        String dataInPresentAndFuture = NOTIFY_JSON + "\n" + NOTIFY_JSON_FUTURE;

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(dataInPresentAndFuture.getBytes()));
        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_TWO))
            .thenReturn(new ByteArrayInputStream(NOTIFY_JSON_PAST.getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON), hasItem(NOTIFY_JSON_FUTURE))),
                eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));
        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON_PAST))), eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.emptyList());

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(OUTPUT_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(OUTPUT_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(jsonlWriterComponent, times(1))
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(targetBlobClient).uploadFromFile(OUTPUT_NAME, true);
        verify(fileWrapper).deleteFileOnExit(OUTPUT_NAME);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    void givenDataOverMaxLines_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        ReflectionTestUtils.setField(underTest, MAX_LINES_FIELD_PROPERTY, "1");

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        String multiLineData = NOTIFY_JSON + "\n" + NOTIFY_JSON + "\n" + NOTIFY_JSON;

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(multiLineData.getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON))), eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(OUTPUT_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(OUTPUT_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(jsonlWriterComponent, times(3))
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(targetBlobClient).uploadFromFile(OUTPUT_NAME, true);
        verify(fileWrapper).deleteFileOnExit(OUTPUT_NAME);
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

        String dataInPresentAndFuture = NOTIFY_JSON + "\n" + NOTIFY_JSON_FUTURE;

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(dataInPresentAndFuture.getBytes()));
        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_TWO))
            .thenReturn(new ByteArrayInputStream("".getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON), hasItem(NOTIFY_JSON_FUTURE))),
                eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(OUTPUT_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(OUTPUT_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(jsonlWriterComponent)
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(targetBlobClient).uploadFromFile(OUTPUT_NAME, true);
        verify(fileWrapper).deleteFileOnExit(OUTPUT_NAME);
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
            .thenReturn(new ByteArrayInputStream(NOTIFY_JSON.getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON))), eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(OUTPUT_NAME)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(OUTPUT_NAME, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, times(1)).create();
        verify(jsonlWriterComponent)
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(targetBlobClient).uploadFromFile(OUTPUT_NAME, true);
        verify(fileWrapper).deleteFileOnExit(OUTPUT_NAME);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenSameFromDate_whenExportBlobDataAndGetUrl_thenCreateContainerAndReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        final OffsetDateTime fromDateSameAsEventDate = OffsetDateTime.of(2000, 01, 29, 0, 0, 0, 0, ZoneOffset.UTC);
        final String datePrefix = "2000-01-29-2001-01-01-";
        final String outputName = datePrefix + NOTIFY_WORKING_FILE_NAME;

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(NOTIFY_JSON.getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON))), eq(fromDateSameAsEventDate), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(outputName)).thenReturn(targetBlobClient);

        String result = underTest
            .exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, fromDateSameAsEventDate, TEST_TO_DATE_TIME);

        assertEquals(outputName, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, times(1)).create();
        verify(jsonlWriterComponent)
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(targetBlobClient).uploadFromFile(outputName, true);
        verify(fileWrapper).deleteFileOnExit(outputName);
        verify(bufferedWriter, times(1)).close();
    }

    @Test
    public void givenSameToDate_whenExportBlobDataAndGetUrl_thenCreateContainerAndReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        final OffsetDateTime toDateSameAsEventDate = OffsetDateTime.of(2000, 01, 29, 0, 0, 0, 0, ZoneOffset.UTC);
        final String datePrefix = "1999-12-01-2000-01-29-";
        final String outputName = datePrefix + NOTIFY_WORKING_FILE_NAME;

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(NOTIFY_JSON.getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON))), eq(TEST_FROM_DATE_TIME), eq(toDateSameAsEventDate)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(false);

        BlobClient targetBlobClient = mock(BlobClient.class);

        when(targetBlobContainerClient.getBlobClient(outputName)).thenReturn(targetBlobClient);

        String result = underTest
            .exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, toDateSameAsEventDate);

        assertEquals(outputName, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, times(1)).create();
        verify(jsonlWriterComponent)
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(targetBlobClient).uploadFromFile(outputName, true);
        verify(fileWrapper).deleteFileOnExit(outputName);
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
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream("Anything".getBytes()));

        doThrow(new IOException("Broken write.")).when(bufferedWriter).write(anyString());

        doAnswer((Answer<Void>) invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("Should throw IOException");
            return null;
        }).when(jsonlWriterComponent).writeLinesAsJsonl(any(), any());

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

    @Test
    public void givenBlobMetadataDoesNotPassFilter_whenExportBlobData_thenReturnNull() {
        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItemOne = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItemOne));

        when(blobItemOne.getName()).thenReturn(TEST_BLOB_NAME_ONE);
        when(blobItemOne.getMetadata()).thenReturn(Collections.emptyMap());

        when(metadataFilterComponent.filterByMetadata(Collections.emptyMap())).thenReturn(false);

        assertEquals(
            null,
            underTest.exportBlobsAndGetOutputName(sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Expected null output when no query matching data found.");
    }

    @Test
    public void givenExtractionWithArchiveEnabled_whenExportBlobDataAndGetUrl_thenReturnUrlOfUploadedExtractedDataBlob() throws Exception {
        ReflectionTestUtils.setField(underTest, ARCHIVE_FLAG_PROPERTY, "true");

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);

        when(sourceBlobServiceClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        when(blobContainerItem.getName()).thenReturn(TEST_CONTAINER_NAME);

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

        when(sourceBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);

        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        when(blobItem.getName()).thenReturn(TEST_BLOB_NAME_ONE);

        when(blobDownloadComponent.openBlobInputStream(sourceBlobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME_ONE))
            .thenReturn(new ByteArrayInputStream(NOTIFY_JSON.getBytes()));

        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(NOTIFY_JSON))), eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(NOTIFY_OUTPUT));

        BlobContainerClient targetBlobContainerClient = mock(BlobContainerClient.class);

        when(targetBlobServiceClient.getBlobContainerClient(TEST_OUTPUT_CONTAINER_NAME)).thenReturn(targetBlobContainerClient);
        when(targetBlobContainerClient.exists()).thenReturn(true);

        BlobClient targetBlobClient = mock(BlobClient.class);

        String archiveOutputName = TEST_DATE_PREFIX + NOTIFY_WORKING_ARCHIVE;

        when(targetBlobContainerClient.getBlobClient(archiveOutputName)).thenReturn(targetBlobClient);

        String result = underTest.exportBlobsAndGetOutputName(
            sourceBlobServiceClient, targetBlobServiceClient, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME
        );

        assertEquals(archiveOutputName, result, OUTPUT_ASSERTION_MATCHING_ERROR);

        verify(targetBlobContainerClient, never()).create();
        verify(jsonlWriterComponent, times(1))
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(NOTIFY_OUTPUT)));
        verify(archiveComponent)
            .createArchive(Collections.singletonList(OUTPUT_NAME), archiveOutputName);
        verify(targetBlobClient).uploadFromFile(archiveOutputName, true);
        verify(fileWrapper, times(1)).deleteFileOnExit(OUTPUT_NAME);
        verify(fileWrapper, times(1)).deleteFileOnExit(archiveOutputName);
        verify(bufferedWriter, times(1)).close();
    }
}