package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_ALLOCATION_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_FEE_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_FEE_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_HISTORY_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_HISTORY_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_REMISSION_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.PAYMENT_REMISSION_TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.cleanUpSingleBlob;

@SpringBootTest(classes = TestConfig.class)
public class PaymentPostDeployTest {

    @Value("${azure.storage-account.export.connection-string}")
    private String exportConnectionString;

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    private BlobServiceClient exportBlobServiceClient;

    @BeforeEach
    public void setUp() {
        // Set up blob clients.
        exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(exportConnectionString);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_HISTORY_EXPORT_CONTAINER_NAME, PAYMENT_HISTORY_TEST_EXPORT_BLOB);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME, PAYMENT_ALLOCATION_TEST_EXPORT_BLOB);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_REMISSION_EXPORT_CONTAINER_NAME, PAYMENT_REMISSION_TEST_EXPORT_BLOB);
        cleanUpSingleBlob(exportBlobServiceClient, PAYMENT_FEE_EXPORT_CONTAINER_NAME, PAYMENT_FEE_TEST_EXPORT_BLOB);
    }

    @Test
    public void givenTestBlob_whenExportBlobData_thenTestBlobsExistInExport() {
        // Verify blob is created in to the export blob storage account.
        assertTrue(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_HISTORY_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_HISTORY_TEST_EXPORT_BLOB)
                        .exists(), "History export blob should have been created.");

        assertTrue(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_ALLOCATION_TEST_EXPORT_BLOB)
                        .exists(), "Allocation export blob should have been created.");

        assertTrue(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_REMISSION_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_REMISSION_TEST_EXPORT_BLOB)
                        .exists(), "Remission export blob should have been created.");

        assertTrue(exportBlobServiceClient
                        .getBlobContainerClient(PAYMENT_FEE_EXPORT_CONTAINER_NAME)
                        .getBlobClient(PAYMENT_FEE_TEST_EXPORT_BLOB)
                        .exists(), "Fee export blob should have been created.");
    }
}
