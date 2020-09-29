package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;
import uk.gov.hmcts.reform.mi.miextractionservice.component.sftp.SftpExportComponentImpl;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.azure.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.service.export.ExportService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.EXPORT_EXTRACTION_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXTRACTION_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXTRACTION_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_JSONL;

@SuppressWarnings({"unchecked","PMD.AvoidUsingHardCodedIP","PMD.ExcessiveImports"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ExportTest {

    private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";

    private static final String SFTP_SERVER_IMAGE = "atmoz/sftp:alpine";
    private static final Integer DEFAULT_PORT = 10_000;
    private static final Integer SFTP_PORT = 22;

    private static final String DEFAULT_COMMAND = "azurite -l /data --blobHost 0.0.0.0 --loose";
    private static final String DEFAULT_CONN_STRING = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
        + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
        + "BlobEndpoint=http://%s:%d/devstoreaccount1;";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String EXTRACT_FILE_NAME = "test-1970-01-01-1970-01-02.jsonl.gz";
    private static final String TEST_VERIFICATION_FILENAME = "tmp_" + TEST_EXPORT_BLOB;

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    @Container
    private static final GenericContainer STAGING_CONTAINER =
        new GenericContainer(AZURITE_IMAGE)
            .withCommand(DEFAULT_COMMAND)
            .withExposedPorts(DEFAULT_PORT);


    @Container
    private static final GenericContainer SFTP_SERVER_CONTAINER =
        new GenericContainer(SFTP_SERVER_IMAGE)
            .withCommand("user:password:::upload")
            .withExposedPorts(SFTP_PORT);


    @Container
    private static final GenericContainer EXPORT_CONTAINER =
        new GenericContainer(AZURITE_IMAGE)
            .withCommand(DEFAULT_COMMAND)
            .withExposedPorts(DEFAULT_PORT);

    private BlobServiceClient stagingBlobServiceClient;
    private BlobServiceClient exportBlobServiceClient;

    @Autowired
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Autowired
    private ExportService classToTest;

    @Autowired
    private SftpExportComponentImpl sftpExportComponent;

    @BeforeEach
    public void setUp() throws Exception {
        STAGING_CONTAINER.start();
        EXPORT_CONTAINER.start();
        SFTP_SERVER_CONTAINER.start();

        Integer stagingPort = STAGING_CONTAINER.getMappedPort(DEFAULT_PORT);
        Integer exportPort = EXPORT_CONTAINER.getMappedPort(DEFAULT_PORT);
        Integer sftpPort = SFTP_SERVER_CONTAINER.getMappedPort(SFTP_PORT);

        stagingBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, stagingPort));

        exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, exportPort));

        ReflectionTestUtils.setField(sftpExportComponent, "port", sftpPort);
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "clientId", null);
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
            "stagingConnString", String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, stagingPort));
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
            "exportConnString", String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, exportPort));
    }

    @AfterEach
    public void tearDown() {
        STAGING_CONTAINER.stop();
        EXPORT_CONTAINER.stop();
        SFTP_SERVER_CONTAINER.stop();
        // Cleanup local created files
        File exportZip = new File(TEST_EXPORT_BLOB);
        File exportFile = new File(EXTRACT_FILE_NAME);
        File verificationZip  = new File(TEST_VERIFICATION_FILENAME);

        if (exportZip.exists()) {
            exportZip.delete();
        }

        if (exportFile.exists()) {
            exportFile.delete();
        }

        if (verificationZip.exists()) {
            verificationZip.delete();
        }
    }

    @Test
    public void givenTestContainer_whenExportBlob_thenArchivedBlobIsCreatedInExportAndEmailIsSent() throws Exception {
        byte[] inputData = TEST_JSONL.getBytes();
        InputStream testStream = new ByteArrayInputStream(inputData);
        InputStream extractionStream = new ByteArrayInputStream(inputData);

        stagingBlobServiceClient
            .createBlobContainer(TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(testStream, inputData.length);

        stagingBlobServiceClient
            .createBlobContainer(TEST_EXTRACTION_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(extractionStream, inputData.length);

        classToTest.exportData();

        assertTrue(exportBlobServiceClient.getBlobContainerClient(EXPORT_CONTAINER_NAME).exists(),
                   "Export container should have been created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(EXPORT_CONTAINER_NAME).getBlobClient(TEST_EXPORT_BLOB).exists(),
                   "Test blob should have been created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(EXPORT_EXTRACTION_CONTAINER_NAME).exists(),
                   "Export extraction date container should have been created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(EXPORT_EXTRACTION_CONTAINER_NAME)
                       .getBlobClient(TEST_EXTRACTION_EXPORT_BLOB).exists(),
                   "Test extraction date blob should have been created.");

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(TEST_EXPORT_BLOB))) {
            exportBlobServiceClient.getBlobContainerClient(EXPORT_CONTAINER_NAME).getBlobClient(TEST_EXPORT_BLOB).download(outputStream);
        }

        ZipFile zipFile = new ZipFile(TEST_EXPORT_BLOB);
        zipFile.extractFile(EXTRACT_FILE_NAME, ".");

        assertTrue(new File(EXTRACT_FILE_NAME).exists(), "Expected archived file to be extracted.");

        try (InputStream inputStream = Files.newInputStream(Paths.get(EXTRACT_FILE_NAME));
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {

            // Newline gets appended to end of file when exporting.
            assertEquals(TEST_JSONL + "\n", new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8),
                         "Data for decompressed gzip file should match input string.");
        }
        sftpExportComponent.loadFile(TEST_EXPORT_BLOB, TEST_VERIFICATION_FILENAME);

        File verificationFile = new File(TEST_VERIFICATION_FILENAME);
        assertTrue(FileUtils.contentEquals(verificationFile, new File(TEST_EXPORT_BLOB)), "Should send file to sftp server");
    }

    @Test
    public void givenInvalidConnectionString_whenExportBlob_thenExceptionIsThrown() {
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "stagingConnString", "InvalidConnectionString");

        assertThrows(Exception.class, () -> classToTest.exportData());

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(),
            "Container was unexpectedly created with invalid connection string.");
    }

    @Test
    public void givenInvalidManagedIdentity_whenExportBlob_thenExceptionIsThrown() {
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "clientId", "InvalidIdentity");

        assertThrows(Exception.class, () -> classToTest.exportData());

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(),
            "Container was unexpectedly created with wrong managed identity.");
    }
}
