package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobMessageBuilderComponent;

@Component
@ConditionalOnProperty(prefix = "mail.sas", name = "enabled", havingValue = "false")
public class UrlBlobMessageBuilderComponentImpl implements BlobMessageBuilderComponent {

    private static final String AZURE_PORTAL_URL = "https://portal.azure.com";
    private static final String BODY = "Please visit %s. The output can be found in the <%s> storage account, <%s> container with the name <%s>";

    @Override
    public String buildMessage(BlobServiceClient blobServiceClient, String containerName, String blobName) {
        return String.format(BODY, AZURE_PORTAL_URL, blobServiceClient.getAccountName(), containerName, blobName);
    }
}
