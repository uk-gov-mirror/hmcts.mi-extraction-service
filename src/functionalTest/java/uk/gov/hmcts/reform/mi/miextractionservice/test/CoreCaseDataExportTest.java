package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.CCD_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CCD_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXPORT_BLOB;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
@SpringBootTest(classes = TestConfig.class)
public class CoreCaseDataExportTest {

    private static final String AZURITE_IMAGE = String.format("%s/mi-azurite", System.getenv("AZURE_CONTAINER_REGISTRY"));
    private static final String SMTP_MOCK_IMAGE = "greenmail/standalone";

    private static final Integer DEFAULT_PORT = 10_000;
    private static final Integer DEFAULT_SMTP_PORT = 3025;

    private static final String DEFAULT_COMMAND = "azurite -l /data --blobHost 0.0.0.0 --loose";
    private static final String DEFAULT_CONN_STRING = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
        + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
        + "BlobEndpoint=http://%s:%d/devstoreaccount1;";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String STAGING_HOST = "localhost1";
    private static final String EXPORT_HOST = "localhost2";
    private static final String MAIL_HOST = "smtpmock";

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    @ClassRule
    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final GenericContainer STAGING_CONTAINER =
        new GenericContainer(AZURITE_IMAGE)
            .withCommand(DEFAULT_COMMAND)
            .withNetwork(NETWORK)
            .withNetworkAliases(STAGING_HOST)
            .withExposedPorts(DEFAULT_PORT);

    @Container
    private static final GenericContainer EXPORT_CONTAINER =
        new GenericContainer(AZURITE_IMAGE)
            .withCommand(DEFAULT_COMMAND)
            .withNetwork(NETWORK)
            .withNetworkAliases(EXPORT_HOST)
            .withExposedPorts(DEFAULT_PORT);

    @Container
    private static final GenericContainer MAIL_SERVER_CONTAINER =
        new GenericContainer(SMTP_MOCK_IMAGE)
            .withNetwork(NETWORK)
            .withNetworkAliases(MAIL_HOST)
            .withExposedPorts(DEFAULT_SMTP_PORT);

    @Container
    private static final GenericContainer UNDER_TEST =
        new GenericContainer(new ImageFromDockerfile().withFileFromPath(".", Path.of(".")))
            .withNetwork(NETWORK);

    private BlobServiceClient stagingBlobServiceClient;
    private BlobServiceClient exportBlobServiceClient;

    @BeforeEach
    public void setUp() throws Exception {
        STAGING_CONTAINER.start();
        EXPORT_CONTAINER.start();
        MAIL_SERVER_CONTAINER.start();

        Integer stagingPort = STAGING_CONTAINER.getMappedPort(DEFAULT_PORT);
        Integer exportPort = EXPORT_CONTAINER.getMappedPort(DEFAULT_PORT);

        stagingBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, stagingPort));

        exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, DEFAULT_HOST, exportPort));

        UNDER_TEST.waitingFor(Wait.forLogMessage(".*Finished application runner.*", 1));

        // Initialise environment variables before each test
        UNDER_TEST.setEnv(new ArrayList<>());

        UNDER_TEST.addEnv("SPRING_MAIL_HOST", MAIL_HOST);
        UNDER_TEST.addEnv("SPRING_MAIL_PORT", String.valueOf(DEFAULT_SMTP_PORT));
        UNDER_TEST.addEnv("SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH", "false");

        UNDER_TEST.addEnv("STORAGE_STAGING_CONNECTION_STRING", String.format(DEFAULT_CONN_STRING, STAGING_HOST, DEFAULT_PORT));
        UNDER_TEST.addEnv("STORAGE_EXPORT_CONNECTION_STRING", String.format(DEFAULT_CONN_STRING, EXPORT_HOST, DEFAULT_PORT));
    }

    @AfterEach
    public void tearDown() {
        STAGING_CONTAINER.stop();
        EXPORT_CONTAINER.stop();
        MAIL_SERVER_CONTAINER.stop();
    }

    @Test
    public void givenTestContainer_whenCopyToLanding_thenTestContainerIsCopiedOver() throws Exception {
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

        UNDER_TEST.addEnv("ARCHIVE_PASSWORD", "testPassword");
        UNDER_TEST.addEnv("RETRIEVE_FROM_DATE", "1970-01-01");
        UNDER_TEST.addEnv("RETRIEVE_TO_DATE", "1970-01-02");
        UNDER_TEST.addEnv("MAIL_TARGETS", "TestMailAddress");

        UNDER_TEST.start();

        assertTrue(exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME).exists(), "No container was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(CCD_EXPORT_CONTAINER_NAME)
                .getBlobClient(TEST_EXPORT_BLOB).exists(), "No first blob was created.");

        /**
         * Work in Progress
            GreenMail greenMail = new GreenMail(
                new ServerSetup(MAIL_SERVER_CONTAINER.getMappedPort(DEFAULT_SMTP_PORT), "localhost", "http")
            );

            assertEquals(1, greenMail.getReceivedMessages().length,
                "Should be exactly 1 email sent.");
            assertEquals("TestMailAddress", greenMail.getReceivedMessages()[0].getAllRecipients()[0].toString(),
                "Should have exactly one sender matching passed in TestMailAddress.");
         */

        UNDER_TEST.stop();
    }

    @Test
    public void givenInvalidConnectionString_whenCopyToLanding_thenExceptionIsThrown() {
        UNDER_TEST.waitingFor(Wait.forLogMessage(".*Application run failed.*", 1));
        UNDER_TEST.addEnv("STORAGE_EXTRACTION_CONNECTION_STRING", "InvalidConnectionString");

        UNDER_TEST.start();

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(),
            "Container was unexpectedly created with invalid connection string.");

        UNDER_TEST.stop();
    }

    @Test
    public void givenInvalidManagedIdentity_whenCopyToLanding_thenExceptionIsThrown() {
        UNDER_TEST.waitingFor(Wait.forLogMessage(".*Application run failed.*", 1));
        UNDER_TEST.addEnv("MI_CLIENT_ID", "InvalidIdentity");

        UNDER_TEST.start();

        assertFalse(exportBlobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME).exists(),
            "Container was unexpectedly created with wrong managed identity.");

        UNDER_TEST.stop();
    }
}
