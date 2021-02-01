package uk.gov.hmcts.reform.mi.miextractionservice.service.export;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.component.archive.ArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.compression.CompressionComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.notification.NotifyTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.sftp.SftpExportComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.writer.DataWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.ExportProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.azure.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.test.stubs.PagedIterableStub;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyMethods")
@ExtendWith(SpringExtension.class)
class ExportServiceImplTest {

    private static final String TEST_DATE = "2000-01-01";
    private static final LocalDate TEST_DATE_AS_LOCALDATE = LocalDate.of(2000, 1, 1);
    private static final String ENABLED_SOURCE_1 = "enabled1";
    private static final String ENABLED_SOURCE_2 = "enabled2";
    private static final String DISABLED_SOURCE = "disabled";
    private static final String ENABLED_CONTAINER_1 = "enabled1";
    private static final String BLOB_IN_DATE = "blob-2000-01.jsonl";
    private static final String BLOB_OUT_DATE = "blob-2000-02.jsonl";
    private static final String OUTPUT_BLOB_NAME = "enabled1-2000-01-01-2000-01-01.jsonl";
    private static final String OUTPUT_GZIP_NAME = "enabled1-2000-01-01-2000-01-01.jsonl.gz";
    private static final String OUTPUT_ZIP_NAME = "enabled1-2000-01-01-2000-01-01.zip";
    private static final String TRUE_VALUE = "true";
    private static final String FALSE_VALUE = "false";

    @Mock private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;
    @Mock private ExportProperties exportProperties;
    @Mock private DataWriterComponent dataWriterComponent;
    @Mock private CompressionComponent compressionComponent;
    @Mock private ArchiveComponent archiveComponent;
    @Mock private NotifyTargetsComponent notifyTargetsComponent;
    @Mock private SftpExportComponent sftpExportComponent;


    private ExportServiceImpl classToTest;

    private BlobServiceClient stagingClient;
    private BlobServiceClient exportClient;

    @BeforeEach
    void setUp() {
        stagingClient = mock(BlobServiceClient.class);
        exportClient = mock(BlobServiceClient.class);

        when(extractionBlobServiceClientFactory.getStagingClient()).thenReturn(stagingClient);
        when(extractionBlobServiceClientFactory.getExportClient()).thenReturn(exportClient);

        when(exportClient.listBlobContainers()).thenReturn(new PagedIterableStub<>()); // To pass connectivity check.
    }

    @Test
    void givenIssueWithStagingClientListBlobContainers_whenExportData_thenThrowExceptionOnCheckConnectivity() {
        when(stagingClient.listBlobContainers()).thenReturn(null);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        assertThrows(Exception.class, () -> classToTest.exportData());
        verify(exportProperties, never()).getSources();
    }

    @Test
    void givenIssueWithExportClientListBlobContainers_whenExportData_thenThrowExceptionOnCheckConnectivity() {
        when(exportClient.listBlobContainers()).thenReturn(null);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        assertThrows(Exception.class, () -> classToTest.exportData());
        verify(exportProperties, never()).getSources();
    }

    @Test
    void givenExportServicesWithWhitelist_whenExportData_thenUploadBlobForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        sourcePropertiesMap.put(ENABLED_SOURCE_2, SourceProperties.builder().enabled(true).build());
        sourcePropertiesMap.put(DISABLED_SOURCE, SourceProperties.builder().enabled(false).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        BlobContainerItem blobContainerItem2 = mock(BlobContainerItem.class);
        when(blobContainerItem2.getName()).thenReturn(ENABLED_SOURCE_2);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem, blobContainerItem2));
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        BlobItem outOfDateBlobItem = mock(BlobItem.class);
        when(outOfDateBlobItem.getName()).thenReturn(BLOB_OUT_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem, outOfDateBlobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(false);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_BLOB_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.singletonList(ENABLED_CONTAINER_1),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(stagingClient, never()).getBlobContainerClient(ENABLED_SOURCE_2);
        verify(exportContainer, times(1)).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_BLOB_NAME, true);
        verify(sftpExportComponent, times(1)).copyFile(OUTPUT_BLOB_NAME);
        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExportServicesWithMultipleContainersToCheck_whenExportData_thenUploadBlobForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        BlobContainerItem blobContainerItem2 = mock(BlobContainerItem.class);
        when(blobContainerItem2.getName()).thenReturn(ENABLED_SOURCE_2);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem, blobContainerItem2));
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        BlobItem outOfDateBlobItem = mock(BlobItem.class);
        when(outOfDateBlobItem.getName()).thenReturn(BLOB_OUT_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem, outOfDateBlobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(false);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_BLOB_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(stagingClient, never()).getBlobContainerClient(ENABLED_SOURCE_2);
        verify(exportContainer, times(1)).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_BLOB_NAME, true);
        verify(sftpExportComponent, times(1)).copyFile(OUTPUT_BLOB_NAME);
        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExportServicesWithNoRecordsToExport_whenExportData_thenDoNoUploads() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(0);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportClient, never()).getBlobContainerClient(anyString());
        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExportServicesAndCompressionEnabled_whenExportData_thenUploadArchiveForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(true);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_GZIP_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(TRUE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportContainer, never()).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_GZIP_NAME, true);
        verify(sftpExportComponent, times(1)).copyFile(OUTPUT_GZIP_NAME);
        verify(compressionComponent, times(1)).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExportServicesAndArchiveEnabled_whenExportData_thenUploadArchiveForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(true);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_ZIP_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(FALSE_VALUE, TRUE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportContainer, never()).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_ZIP_NAME, true);
        verify(sftpExportComponent, times(1)).copyFile(OUTPUT_ZIP_NAME);
        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, times(1)).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExportServicesAndArchiveAndCompressionEnabled_whenExportData_thenUploadArchiveForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(true);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_ZIP_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(TRUE_VALUE, TRUE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportContainer, never()).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_ZIP_NAME, true);
        verify(compressionComponent, times(1)).compressFile(anyString(), anyString());
        verify(archiveComponent, times(1)).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExportServicesAndNotificationEnabled_whenExportData_thenUploadArchiveForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(true);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_BLOB_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, TRUE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportContainer, never()).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_BLOB_NAME, true);
        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent).sendMessage(eq("Blob enabled1-2000-01-01-2000-01-01.jsonl uploaded to container enabled1."));
    }

    @Test
    void givenExportServicesPrefixOnContainer_whenExportData_thenUploadBlobForEachEnabledService() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        String containerPrefix = "test-prefix";
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).prefix(containerPrefix).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        when(dataWriterComponent.writeRecordsForDateRange(any(BufferedWriter.class),
                                                          eq(blobClient),
                                                          any(SourceProperties.class),
                                                          eq(TEST_DATE_AS_LOCALDATE),
                                                          eq(TEST_DATE_AS_LOCALDATE))).thenReturn(1);

        BlobContainerClient exportContainer = mock(BlobContainerClient.class);
        when(exportClient.getBlobContainerClient(eq(containerPrefix + "-" + ENABLED_SOURCE_1))).thenReturn(exportContainer);
        when(exportContainer.exists()).thenReturn(true);
        BlobClient exportBlob = mock(BlobClient.class);
        when(exportContainer.getBlobClient(eq(OUTPUT_BLOB_NAME))).thenReturn(exportBlob);

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportContainer, never()).create();
        verify(exportBlob, times(1)).uploadFromFile(OUTPUT_BLOB_NAME, true);
        verify(sftpExportComponent, times(1)).copyFile(OUTPUT_BLOB_NAME);
        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }

    @Test
    void givenExceptionWhenWritingData_whenExportData_thenSkipBlobUpload() {
        Map<String, SourceProperties> sourcePropertiesMap = new ConcurrentHashMap<>();
        sourcePropertiesMap.put(ENABLED_SOURCE_1, SourceProperties.builder().enabled(true).build());
        when(exportProperties.getSources()).thenReturn(sourcePropertiesMap);

        BlobContainerItem blobContainerItem = mock(BlobContainerItem.class);
        when(blobContainerItem.getName()).thenReturn(ENABLED_CONTAINER_1);
        when(stagingClient.listBlobContainers()).thenReturn(new PagedIterableStub<>(blobContainerItem));

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(stagingClient.getBlobContainerClient(ENABLED_CONTAINER_1)).thenReturn(blobContainerClient);

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn(BLOB_IN_DATE);
        when(blobContainerClient.listBlobs()).thenReturn(new PagedIterableStub<>(blobItem));

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB_IN_DATE)).thenReturn(blobClient);

        doAnswer(invocation -> {
            throw new IOException("Write went wrong.");
        }).when(dataWriterComponent).writeRecordsForDateRange(any(BufferedWriter.class),
                                                              eq(blobClient),
                                                              any(SourceProperties.class),
                                                              eq(TEST_DATE_AS_LOCALDATE),
                                                              eq(TEST_DATE_AS_LOCALDATE));

        classToTest = new ExportServiceImpl(FALSE_VALUE, FALSE_VALUE, FALSE_VALUE, Collections.emptyList(),
                                            TEST_DATE, TEST_DATE,
                                            extractionBlobServiceClientFactory, exportProperties,
                                            dataWriterComponent, compressionComponent, archiveComponent, notifyTargetsComponent,
                                            sftpExportComponent);

        classToTest.exportData();

        verify(exportClient, never()).getBlobContainerClient(anyString());

        verify(compressionComponent, never()).compressFile(anyString(), anyString());
        verify(archiveComponent, never()).createArchive(anyList(), anyString());
        verify(notifyTargetsComponent, never()).sendMessage(anyString());
    }
}
