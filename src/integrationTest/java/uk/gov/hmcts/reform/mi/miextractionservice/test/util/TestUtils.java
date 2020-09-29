package uk.gov.hmcts.reform.mi.miextractionservice.test.util;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public final class TestUtils {

    private static long timeout = 120_000L;

    public static void cleanUpTestFiles(BlobServiceClient blobServiceClient, String containerName) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Delete test container if it exists
        if (blobContainerClient.exists()) {
            blobContainerClient.delete();
        }

        do {
            checkTimeout(startTime);

            log.info("Waiting 5 seconds for container deletion");
            Thread.sleep(5_000);
        } while (blobContainerClient.exists());
    }

    public static void cleanUpSingleBlob(BlobServiceClient blobServiceClient, String containerName, String blobName) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);

        // Delete test container if it exists
        if (blobClient.exists()) {
            blobClient.delete();
        }

        do {
            checkTimeout(startTime);

            log.info("Waiting 5 seconds for container deletion");
            Thread.sleep(5_000);
        } while (blobClient.exists());
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public static BlobClient createTestBlob(BlobServiceClient blobServiceClient, String containerName, String blobName)
        throws InterruptedException {

        long startTime = System.currentTimeMillis();

        BlobClient blobClient = null;

        do {
            checkTimeout(startTime);

            try {
                BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

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

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private static void checkTimeout(long startTime) {
        if ((System.currentTimeMillis() - startTime) > timeout) {
            throw new RuntimeException("Timed out trying to create test container.");
        }
    }

    private TestUtils() {
        // Private constructor
    }
}
