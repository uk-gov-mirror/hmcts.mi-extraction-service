package uk.gov.hmcts.reform.mi.miextractionservice.component;

import com.azure.storage.blob.BlobServiceClient;

import java.io.InputStream;

public interface BlobDownloadComponent {
    /**
     * Gets the data of a blob in Azure Storage Account as an inputStream to be read.
     *
     * @param blobServiceClient for the storage account to access.
     * @param containerName of the container to be accessed.
     * @param blobName of the blob to be downloaded.
     * @return inputStream of blob data.
     */
    InputStream openBlobInputStream(BlobServiceClient blobServiceClient, String containerName, String blobName);
}
