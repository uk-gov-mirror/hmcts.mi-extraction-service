package uk.gov.hmcts.reform.mi.miextractionservice.test;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.TestConfig;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.DASH_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.EXPORT_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_BLOB_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CONTAINER_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.util.TestUtils.cleanUpTestFiles;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.util.TestUtils.createTestBlob;

@Slf4j
@SpringBootTest(classes = TestConfig.class)
public class PreDeployTest {

    @Value("${azure.storage-account.staging.connection-string}")
    private String stagingConnectionString;

    @Value("${azure.storage-account.export.connection-string}")
    private String exportConnectionString;

    @Value("${build.version}")
    private String buildVersion;

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    private BlobServiceClient stagingBlobServiceClient;

    private String testContainer;

    @BeforeEach
    public void setUp() throws InterruptedException {
        // Set up blob clients.
        stagingBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(stagingConnectionString);

        BlobServiceClient exportBlobServiceClient = blobServiceClientFactory
            .getBlobClientWithConnectionString(exportConnectionString);

        testContainer = TEST_CONTAINER_NAME + DASH_DELIMITER + buildVersion;

        // Clean any previous leftover test data.
        cleanUpTestFiles(stagingBlobServiceClient, testContainer);
        cleanUpTestFiles(exportBlobServiceClient, buildVersion + DASH_DELIMITER + EXPORT_CONTAINER_NAME);
    }

    @Test
    public void preDeploySetup() throws InterruptedException {
        // Setup throwaway test data.
        byte[] testData = TEST_JSONL.getBytes();
        ByteArrayInputStream inputStreamOne = new ByteArrayInputStream(testData);

        assertFalse(stagingBlobServiceClient
            .getBlobContainerClient(testContainer)
            .getBlobClient(TEST_BLOB_NAME)
            .exists(), "Test blob should not exist in staging storage.");

        // Upload to landing service storage account.
        BlobClient blobClient = createTestBlob(stagingBlobServiceClient, testContainer, TEST_BLOB_NAME);

        blobClient
            .getBlockBlobClient()
            .upload(inputStreamOne, testData.length);

        log.info("Created blob {} in staging container {} for export test",
                 blobClient.getBlobName(), blobClient.getContainerName());

        // Verify blobs exists in source storage account.
        assertTrue(stagingBlobServiceClient
            .getBlobContainerClient(testContainer)
            .getBlobClient(TEST_BLOB_NAME)
            .getBlockBlobClient()
            .exists(), "Blob was not successfully created on staging storage.");
    }
}
