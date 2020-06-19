package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.NOTIFY_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.NOTIFY_TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.NOTIFY_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_NOTIFY_JSONL;

@SuppressWarnings({"unchecked","PMD.AvoidUsingHardCodedIP","PMD.ExcessiveImports"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = TestConfig.class)
public class NotifyDataExportTest {

    private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";

    private static final Integer DEFAULT_PORT = 10_000;

    private static final String DEFAULT_COMMAND = "azurite -l /data --blobHost 0.0.0.0 --loose";
    private static final String DEFAULT_CONN_STRING = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
        + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
        + "BlobEndpoint=http://%s:%d/devstoreaccount1;";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String TEST_MAIL_ADDRESS = "TestMailAddress";
    private static final String EXTRACT_FILE_NAME = "1970-01-01-1970-01-02-NOTIFY_EXTRACT.jsonl";

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

        ReflectionTestUtils.setField(underTest, "dataSource", "Notify");
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "clientId", null);
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
            "stagingConnString", String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, stagingPort));
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
            "exportConnString", String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, exportPort));
    }

    @AfterEach
    public void tearDown() {
        dumbster.stop();

        STAGING_CONTAINER.stop();
        EXPORT_CONTAINER.stop();

        // Cleanup local created files
        File exportZip = new File(TEST_EXPORT_BLOB);
        File exportFile = new File(EXTRACT_FILE_NAME);

        if (exportZip.exists()) {
            exportZip.delete();
        }

        if (exportFile.exists()) {
            exportFile.delete();
        }
    }

    @Test
    public void givenTestContainer_whenExportBlob_thenArchivedBlobIsCreatedInExportAndEmailIsSent() throws Exception {
        byte[] inputData = TEST_NOTIFY_JSONL.getBytes();
        InputStream inputStream = new ByteArrayInputStream(inputData);

        stagingBlobServiceClient
            .createBlobContainer(NOTIFY_TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        assertFalse(exportBlobServiceClient.getBlobContainerClient(NOTIFY_TEST_CONTAINER_NAME).exists(), "Leftover container exists.");
        assertFalse(exportBlobServiceClient.getBlobContainerClient(NOTIFY_TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME).exists(), "Leftover first blob exists.");

        underTest.exportBlobs();

        assertTrue(exportBlobServiceClient.getBlobContainerClient(NOTIFY_EXPORT_CONTAINER_NAME).exists(), "No container was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(NOTIFY_EXPORT_CONTAINER_NAME)
            .getBlobClient(NOTIFY_TEST_EXPORT_BLOB).exists(), "No first blob was created.");

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(NOTIFY_TEST_EXPORT_BLOB))) {
            exportBlobServiceClient.getBlobContainerClient(NOTIFY_EXPORT_CONTAINER_NAME).getBlobClient(NOTIFY_TEST_EXPORT_BLOB)
                .download(outputStream);
        }

        ZipFile zipFile = new ZipFile(NOTIFY_TEST_EXPORT_BLOB);

        zipFile.extractFile(EXTRACT_FILE_NAME, ".");

        assertTrue(new File(EXTRACT_FILE_NAME).exists(), "Expected archived file to be extracted.");

        List<SmtpMessage> receivedEmails = dumbster.getReceivedEmails();
        assertEquals(1, receivedEmails.size(), "Should have receivied only 1 email.");

        SmtpMessage email = receivedEmails.get(0);
        assertEquals(TEST_MAIL_ADDRESS, email.getHeaderValue("To"), "Should have sent an email to TestMailAddress.");
        assertEquals("Management Information Exported Data Url", email.getHeaderValue("Subject"),
            "Should have a static subject message.");
        assertTrue(email.getBody().contains(NOTIFY_TEST_EXPORT_BLOB),
            "Should have output blob name somewhere in email body as part of the generated SAS url.");
    }
}