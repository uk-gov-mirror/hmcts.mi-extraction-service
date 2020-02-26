package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobDownloadComponent;

import java.io.InputStream;

@Component
public class BlobDownloadComponentImpl implements BlobDownloadComponent {

    @Override
    public InputStream openBlobInputStream(BlobServiceClient blobServiceClient, String containerName, String blobName) {
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).openInputStream();
    }
}
