package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
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
import uk.gov.hmcts.reform.mi.miextractionservice.component.ExportBlobDataComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.ExtractionBlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.service.BlobExportService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.LOCALHOST_IP;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_ALLOCATION_TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_ALLOCATION_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_FEE_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_FEE_TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_FEE_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_HISTORY_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_HISTORY_TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_HISTORY_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_REMISSION_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_REMISSION_TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_REMISSION_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_PAYMENT_JSONL;

@SuppressWarnings({"unchecked","PMD.ExcessiveImports"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = TestConfig.class)
public class PaymentDataExportTest {

    private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite";

    private static final Integer DEFAULT_PORT = 10_000;

    private static final String DEFAULT_COMMAND = "azurite -l /data --blobHost 0.0.0.0 --loose";
    private static final String DEFAULT_CONN_STRING = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;"
        + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
        + "BlobEndpoint=http://%s:%d/devstoreaccount1;";

    private static final String TEST_MAIL_ADDRESS = "TestMailAddress";
    private static final String HISTORY_FILE_NAME = "1970-01-01-1970-01-02-payment-history-extract.jsonl";
    private static final String ALLOCATION_FILE_NAME = "1970-01-01-1970-01-02-payment-allocation-extract.jsonl";
    private static final String REMISSION_FILE_NAME = "1970-01-01-1970-01-02-payment-remission-extract.jsonl";
    private static final String FEE_FILE_NAME = "1970-01-01-1970-01-02-payment-fee-extract.jsonl";

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
    private ExportBlobDataComponent exportBlobDataComponent;

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
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, LOCALHOST_IP, stagingPort));

        exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(String.format(DEFAULT_CONN_STRING, LOCALHOST_IP, exportPort));

        ReflectionTestUtils.setField(underTest, "dataSource", "Payment");
        ReflectionTestUtils.setField(exportBlobDataComponent, "archiveFlag", "false");
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory, "clientId", null);
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
                                     "stagingConnString", String.format(DEFAULT_CONN_STRING, LOCALHOST_IP, stagingPort));
        ReflectionTestUtils.setField(extractionBlobServiceClientFactory,
                                     "exportConnString", String.format(DEFAULT_CONN_STRING, LOCALHOST_IP, exportPort));
    }

    @AfterEach
    public void tearDown() {
        dumbster.stop();

        STAGING_CONTAINER.stop();
        EXPORT_CONTAINER.stop();

        // Cleanup local created files
        final File exportHistoryFile = new File(HISTORY_FILE_NAME);
        final File exportAllocationFile = new File(ALLOCATION_FILE_NAME);
        final File exportRemissionFile = new File(REMISSION_FILE_NAME);
        final File exportFeeFile = new File(FEE_FILE_NAME);

        if (exportHistoryFile.exists()) {
            exportHistoryFile.delete();
        }
        if (exportAllocationFile.exists()) {
            exportAllocationFile.delete();
        }
        if (exportRemissionFile.exists()) {
            exportRemissionFile.delete();
        }
        if (exportFeeFile.exists()) {
            exportFeeFile.delete();
        }
    }

    @Test
    public void givenPaymentContainers_whenExportBlob_thenArchivedBlobIsCreatedInExportAndEmailIsSent() throws Exception {
        byte[] inputData = TEST_PAYMENT_JSONL.getBytes();

        InputStream inputStream = new ByteArrayInputStream(inputData);
        stagingBlobServiceClient
            .createBlobContainer(PAYMENT_HISTORY_TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        inputStream = new ByteArrayInputStream(inputData);
        stagingBlobServiceClient
            .createBlobContainer(PAYMENT_ALLOCATION_TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        inputStream = new ByteArrayInputStream(inputData);
        stagingBlobServiceClient
            .createBlobContainer(PAYMENT_REMISSION_TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        inputStream = new ByteArrayInputStream(inputData);
        stagingBlobServiceClient
            .createBlobContainer(PAYMENT_FEE_TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStream, inputData.length);

        assertFalse(exportBlobServiceClient.getBlobContainerClient(PAYMENT_HISTORY_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_HISTORY_TEST_EXPORT_BLOB).exists(), "Leftover history blob exists.");
        assertFalse(exportBlobServiceClient.getBlobContainerClient(PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_ALLOCATION_TEST_EXPORT_BLOB).exists(), "Leftover allocation blob exists.");
        assertFalse(exportBlobServiceClient.getBlobContainerClient(PAYMENT_REMISSION_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_REMISSION_TEST_EXPORT_BLOB).exists(), "Leftover remission blob exists.");
        assertFalse(exportBlobServiceClient.getBlobContainerClient(PAYMENT_FEE_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_FEE_TEST_EXPORT_BLOB).exists(), "Leftover fee blob exists.");

        underTest.exportBlobs();

        assertTrue(exportBlobServiceClient.getBlobContainerClient(PAYMENT_HISTORY_EXPORT_CONTAINER_NAME)
                       .getBlobClient(PAYMENT_HISTORY_TEST_EXPORT_BLOB).exists(), "No history blob was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME)
                       .getBlobClient(PAYMENT_ALLOCATION_TEST_EXPORT_BLOB).exists(), "No allocation blob was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(PAYMENT_REMISSION_EXPORT_CONTAINER_NAME)
                       .getBlobClient(PAYMENT_REMISSION_TEST_EXPORT_BLOB).exists(), "No remission blob was created.");
        assertTrue(exportBlobServiceClient.getBlobContainerClient(PAYMENT_FEE_EXPORT_CONTAINER_NAME)
                       .getBlobClient(PAYMENT_FEE_TEST_EXPORT_BLOB).exists(), "No fee blob was created.");

        List<SmtpMessage> receivedEmails = dumbster.getReceivedEmails();
        assertEquals(1, receivedEmails.size(), "Should have received only 1 email.");

        SmtpMessage email = receivedEmails.get(0);
        assertEquals(TEST_MAIL_ADDRESS, email.getHeaderValue("To"), "Should have sent an email to TestMailAddress.");
        assertEquals("Management Information Exported Data Url", email.getHeaderValue("Subject"),
                     "Should have a static subject message.");
        assertTrue(email.getBody().contains(PAYMENT_HISTORY_TEST_EXPORT_BLOB),
                   "Should have history blob name somewhere in email body as part of the generated SAS url.");
        assertTrue(email.getBody().contains(PAYMENT_ALLOCATION_TEST_EXPORT_BLOB),
                   "Should have allocation blob name somewhere in email body as part of the generated SAS url.");
        assertTrue(email.getBody().contains(PAYMENT_REMISSION_TEST_EXPORT_BLOB),
                   "Should have remission blob name somewhere in email body as part of the generated SAS url.");
        assertTrue(email.getBody().contains(PAYMENT_FEE_TEST_EXPORT_BLOB),
                   "Should have fee blob name somewhere in email body as part of the generated SAS url.");
    }
}
