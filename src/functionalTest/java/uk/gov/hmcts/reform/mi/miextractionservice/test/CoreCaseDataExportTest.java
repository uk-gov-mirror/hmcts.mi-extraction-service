package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;
import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.CCD_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CCD_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXPORT_BLOB_ARCHIVE;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXPORT_BLOB_FILE;

@SuppressWarnings({"unchecked","PMD.AvoidUsingHardCodedIP","PMD.ExcessiveImports"})
@SpringBootTest(classes = TestConfig.class)
public class CoreCaseDataExportTest {

    private static final String AZURITE_IMAGE = String.format("%s/mi-azurite", System.getenv("AZURE_CONTAINER_REGISTRY"));

    private static final Integer DEFAULT_PORT = 10_000;

    private static final String DEFAULT_COMMAND = "azurite -l /data --blobHost 0.0.0.0 --loose";
    private static final String DEFAULT_CONN_STRING = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
        + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
        + "BlobEndpoint=http://%s:%d/devstoreaccount1;";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String TEST_MAIL_ADDRESS = "TestMailAddress";

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    @Container
    private static final GenericContainer STAGING_CONTAINER =
        new GenericContainer(AZURITE_IMAGE)
            .withCommand(DEFAULT_COMMAND)
            .withExposedPorts(DEFAULT_PORT);

    @Container
    private static final GenericContainer EXPORT_CONTAINER =
        new GenericContainer(AZURITE_IMAGE)
            .withCommand(DEFAULT_COMMAND)
            .withExposedPorts(DEFAULT_PORT);

    private BlobServiceClient stagingBlobServiceClient;
    private BlobServiceClient exportBlobServiceClient;

    private SimpleSmtpServer dumbster;

    @Autowired
    private EmailBlobUrlToTargetsComponent emailBlobUrlToTargetsComponent;

    @Autowired
    @Qualifier("ccd")
    private ExportBlobDataComponent ccdExportBlobDataComponent;

    @Autowired
    private ExtractionBlobServiceClientFactory extractionBlobServiceClientFactory;

    @Autowired
    private BlobExportService underTest;

    @BeforeEach
    public void setUp() throws Exception {
        dumbster = SimpleSmtpServer.start(TestConfig.mailerPort);

        STAGING_CONTAINER.start();
        EXPORT_CONTAINER.start();

        Integer stagingPort = STAGING_CONTAINER.getMappedPort(DEFAULT_PORT);
        Integer exportPort = EXPORT_CONTAINER.getMappedPort(DEFAULT_PORT);

        stagingBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, stagingPort));

        exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, exportPort));

        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
            "stagingConnString", String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, stagingPort));
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
            "exportConnString", String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, exportPort));

        ReflectionTestUtils.setField(emailBlobUrlToTargetsComponent, "targets", "");
        ReflectionTestUtils.setField(ccdExportBlobDataComponent, "archiveFlag", "false");
    }

    @AfterEach
    public void tearDown() {
        dumbster.stop();

        STAGING_CONTAINER.stop();
        EXPORT_CONTAINER.stop();

        // Cleanup local created files
        File exportZip = new File(TEST_EXPORT_BLOB_ARCHIVE);
        File exportFile = new File(TEST_EXPORT_BLOB_FILE);

        if (exportZip.exists()) {
            exportZip.delete();
        }

        if (exportFile.exists()) {
            exportFile.delete();
        }
    }

    @Test
    public void givenTestContainer_whenExportBlob_thenArchivedBlobIsCreatedInExportAndEmailIsSent() throws Exception {
        byte[] inputData = TEST_CCD_JSONL.getBytes();
        InputStream inputStream = new ByteArrayInputStream(inputData);

        stagingBlobServiceClient
            .createBlobContainer(TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(), "Leftover container exists.");
        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME).exists(), "Leftover first blob exists.");

        underTest.exportBlobs();

        assertTrue(exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME).exists(), "No container was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME)
            .getBlobClient(TEST_EXPORT_BLOB_FILE).exists(), "No first blob was created.");

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(TEST_EXPORT_BLOB_FILE))) {
            exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME).getBlobClient(TEST_EXPORT_BLOB_FILE).download(outputStream);
        }

        assertTrue(new File(TEST_EXPORT_BLOB_FILE).exists(), "Expected jsonl file to be downloaded.");
    }

    @Test
    public void givenTestContainerWithArchiveAndMailEnabled_whenExportBlob_thenArchivedBlobIsCreatedInExportAndEmailIsSent() throws Exception {
        ReflectionTestUtils.setField(emailBlobUrlToTargetsComponent, "targets", TEST_MAIL_ADDRESS);
        ReflectionTestUtils.setField(ccdExportBlobDataComponent, "archiveFlag", "true");

        byte[] inputData = TEST_CCD_JSONL.getBytes();
        InputStream inputStream = new ByteArrayInputStream(inputData);

        stagingBlobServiceClient
            .createBlobContainer(TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(), "Leftover container exists.");
        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME).exists(), "Leftover first blob exists.");

        underTest.exportBlobs();

        assertTrue(exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME).exists(), "No container was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME)
            .getBlobClient(TEST_EXPORT_BLOB_ARCHIVE).exists(), "No first blob was created.");

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(TEST_EXPORT_BLOB_ARCHIVE))) {
            exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME).getBlobClient(TEST_EXPORT_BLOB_ARCHIVE).download(outputStream);
        }

        ZipFile zipFile = new ZipFile(TEST_EXPORT_BLOB_ARCHIVE);

        zipFile.extractFile(TEST_EXPORT_BLOB_FILE, ".");

        assertTrue(new File(TEST_EXPORT_BLOB_FILE).exists(), "Expected archived file to be extracted.");

        List<SmtpMessage> receivedEmails = dumbster.getReceivedEmails();
        assertEquals(1, receivedEmails.size(), "Should have receivied only 1 email.");

        SmtpMessage email = receivedEmails.get(0);
        assertEquals(TEST_MAIL_ADDRESS, email.getHeaderValue("To"), "Should have sent an email to TestMailAddress.");
        assertEquals("Management Information Exported Data Url", email.getHeaderValue("Subject"),
            "Should have a static subject message.");
        assertTrue(email.getBody().contains(TEST_EXPORT_BLOB_ARCHIVE),
            "Should have output blob name somewhere in email body as part of the generated SAS url.");
    }

    @Test
    public void givenInvalidConnectionString_whenExportBlob_thenExceptionIsThrown() {
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "stagingConnString", "InvalidConnectionString");

        assertThrows(Exception.class, () -> underTest.exportBlobs());

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(),
            "Container was unexpectedly created with invalid connection string.");
    }

    @Test
    public void givenInvalidManagedIdentity_whenExportBlob_thenExceptionIsThrown() {
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "clientId", "InvalidIdentity");

        assertThrows(Exception.class, () -> underTest.exportBlobs());

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(),
            "Container was unexpectedly created with wrong managed identity.");
    }
}
