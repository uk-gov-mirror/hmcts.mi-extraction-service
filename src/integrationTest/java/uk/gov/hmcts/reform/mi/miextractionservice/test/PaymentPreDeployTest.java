package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.cleanUpSingleBlob;
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.cleanUpTestFiles;
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.createTestBlob;

@SpringBootTest(classes = TestConfig.class)
public class PaymentPreDeployTest {

    @Value("${azure.storage-account.staging.connection-string}")
    private String stagingConnectionString;

    @Value("${azure.storage-account.export.connection-string}")
    private String exportConnectionString;

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    private BlobServiceClient stagingBlobServiceClient;
    private BlobServiceClient exportBlobServiceClient;

    @BeforeEach
    public void setUp() throws InterruptedException {
        // Set up blob clients.
        stagingBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(stagingConnectionString);

        exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(exportConnectionString);

        // Clean any previous leftover test data.
        cleanUpTestFiles(stagingBlobServiceClient, PAYMENT_HISTORY_TEST_CONTAINER_NAME);
        cleanUpTestFiles(stagingBlobServiceClient, PAYMENT_ALLOCATION_TEST_CONTAINER_NAME);
        cleanUpTestFiles(stagingBlobServiceClient, PAYMENT_REMISSION_TEST_CONTAINER_NAME);
        cleanUpTestFiles(stagingBlobServiceClient, PAYMENT_FEE_TEST_CONTAINER_NAME);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_HISTORY_EXPORT_CONTAINER_NAME, PAYMENT_HISTORY_TEST_EXPORT_BLOB);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME, PAYMENT_ALLOCATION_TEST_EXPORT_BLOB);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_REMISSION_EXPORT_CONTAINER_NAME, PAYMENT_REMISSION_TEST_EXPORT_BLOB);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_FEE_EXPORT_CONTAINER_NAME, PAYMENT_FEE_TEST_EXPORT_BLOB);
    }

    @Test
    public void preDeploySetup() throws InterruptedException {
        // Setup throwaway test data.
        byte[] testData = TEST_PAYMENT_JSONL.getBytes();
        ByteArrayInputStream historyStream = new ByteArrayInputStream(testData);
        ByteArrayInputStream allocationStream = new ByteArrayInputStream(testData);
        ByteArrayInputStream remissionStream = new ByteArrayInputStream(testData);
        ByteArrayInputStream feeStream = new ByteArrayInputStream(testData);

        // Upload to landing service storage account.
        createTestBlob(stagingBlobServiceClient, PAYMENT_HISTORY_TEST_CONTAINER_NAME, TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(historyStream, testData.length, true);

        // Upload to landing service storage account.
        createTestBlob(stagingBlobServiceClient, PAYMENT_ALLOCATION_TEST_CONTAINER_NAME, TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(allocationStream, testData.length, true);

        // Upload to landing service storage account.
        createTestBlob(stagingBlobServiceClient, PAYMENT_REMISSION_TEST_CONTAINER_NAME, TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(remissionStream, testData.length, true);

        // Upload to landing service storage account.
        createTestBlob(stagingBlobServiceClient, PAYMENT_FEE_TEST_CONTAINER_NAME, TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(feeStream, testData.length, true);

        // Verify no target blobs created yet
        assertFalse(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_HISTORY_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_HISTORY_TEST_EXPORT_BLOB)
                        .exists(), "History export blob should not exist yet.");

        assertFalse(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_ALLOCATION_TEST_EXPORT_BLOB)
                        .exists(), "Allocation export blob should not exist yet.");

        assertFalse(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_REMISSION_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_REMISSION_TEST_EXPORT_BLOB)
                        .exists(), "Remission export blob should not exist yet.");

        assertFalse(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_FEE_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_FEE_TEST_EXPORT_BLOB)
                        .exists(), "Fee export blob should not exist yet.");
    }
}
