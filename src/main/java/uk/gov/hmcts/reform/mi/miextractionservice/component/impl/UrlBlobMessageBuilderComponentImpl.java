package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobMessageBuilderComponent;

@Component
@ConditionalOnProperty(prefix = "mail.sas", name = "enabled", havingValue = "false")
public class UrlBlobMessageBuilderComponentImpl implements BlobMessageBuilderComponent {

    @Override
    public String buildMessage(BlobServiceClient blobServiceClient, String containerName, String blobName) {
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).getBlobUrl();
    }
}
