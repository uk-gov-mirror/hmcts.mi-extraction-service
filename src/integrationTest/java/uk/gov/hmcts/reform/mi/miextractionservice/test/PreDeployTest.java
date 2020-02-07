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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.CCD_EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CCD_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_EXPORT_BLOB;
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.cleanUpSingleBlob;
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.cleanUpTestFiles;
import static uk.gov.hmcts.reform.mi.miextractionservice.util.TestUtils.createTestBlob;

@SpringBootTest(classes = TestConfig.class)
public class PreDeployTest {

    @Value("${azure.storage-account.staging.connection-string}")
    private String stagingConnectionString;

    @Value("${azure.storage-account.export.connection-string}")
    private String exportConnectionString;

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    private BlobServiceClient stagingBlobServiceClient;

    @BeforeEach
    public void setUp() {
        // Set up blob clients.
        stagingBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(stagingConnectionString);

        BlobServiceClient exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(exportConnectionString);

        // Clean any previous leftover test data.
        cleanUpTestFiles(stagingBlobServiceClient, TEST_CONTAINER_NAME);
        cleanUpSingleBlob(exportBlobServiceClient, CCD_EXPORT_CONTAINER_NAME, TEST_EXPORT_BLOB);
    }

    @Test
    public void preDeploySetup() throws InterruptedException {
        // Setup throwaway test data.
        byte[] testData = TEST_CCD_JSONL.getBytes();
        ByteArrayInputStream inputStreamOne = new ByteArrayInputStream(testData);

        // Upload to landing service storage account.
        createTestBlob(stagingBlobServiceClient, TEST_BLOB_NAME)
            .getBlockBlobClient()
            .upload(inputStreamOne, testData.length);

        // Verify blobs exists in source storage account.
        assertTrue(stagingBlobServiceClient
            .getBlobContainerClient(TEST_CONTAINER_NAME)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .exists(), "Blob was not successfully created on landing storage.");
    }
}
