package uk.gov.hmcts.reform.mi.miextractionservice.component;

import com.azure.storage.blob.BlobServiceClient;

public interface GenerateBlobUrlComponent {

    String generateUrlForBlob(BlobServiceClient blobServiceClient, String containerName, String blobName);
}
