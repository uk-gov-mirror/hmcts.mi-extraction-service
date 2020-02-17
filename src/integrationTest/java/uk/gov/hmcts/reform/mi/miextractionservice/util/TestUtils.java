package uk.gov.hmcts.reform.mi.miextractionservice.util;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static uk.gov.hmcts.reform.mi.miextractionservice.data.TestConstants.TEST_CONTAINER_NAME;

@Slf4j
public final class TestUtils {

    public static void cleanUpTestFiles(BlobServiceClient blobServiceClient, String containerName) throws Exception {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Delete test container if it exists
        if (blobContainerClient.exists()) {
            blobContainerClient.delete();
        }

        while (blobContainerClient.exists()) {
            log.info("Waiting 5 seconds for container deletion");
            Thread.sleep(5_000);
        }
    }

    public static void cleanUpSingleBlob(BlobServiceClient blobServiceClient, String containerName, String blobName) throws Exception {
        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);

        // Delete test container if it exists
        if (blobClient.exists()) {
            blobClient.delete();
        }

        while (blobClient.exists()) {
            log.info("Waiting 5 seconds for container deletion");
            Thread.sleep(5_000);
        }
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidThrowingRawExceptionTypes"})
    public static BlobClient createTestBlob(BlobServiceClient blobServiceClient, String blobName) throws InterruptedException {
        long timeout = 120_000;
        long startTime = System.currentTimeMillis();

        BlobClient blobClient = null;

        do {
            if ((System.currentTimeMillis() - startTime) > timeout) {
                throw new RuntimeException("Timed out trying to create test container.");
            }

            try {
                BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME);

                if (blobContainerClient.exists()) {
                    blobClient = blobContainerClient.getBlobClient(blobName);
                } else {
                    blobContainerClient.create();
                }
            } catch (Exception e) {
                log.info("Waiting 5 seconds for creation");
                // Wait 5s before trying again
                Thread.sleep(5_000);
            }
        } while (Objects.isNull(blobClient));

        return blobClient;
    }

    private TestUtils() {
        // Private constructor
    }
}
