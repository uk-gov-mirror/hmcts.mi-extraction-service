package uk.gov.hmcts.reform.mi.miextractionservice.component;

import com.azure.storage.blob.BlobServiceClient;

public interface BlobSasMessageBuilderComponent {

    String buildMessage(BlobServiceClient blobServiceClient, String containerName, String blobName);
}
