package uk.gov.hmcts.reform.mi.miextractionservice.factory;

import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;

@Slf4j
@Component
public class ExtractionBlobServiceClientFactory {

    @Value("${azure.managed-identity.client-id}")
    private String clientId;

    @Value("${azure.storage-account.staging.name}")
    private String stagingName;

    @Value("${azure.storage-account.export.name}")
    private String exportName;

    @Value("${azure.storage-account.staging.connection-string}")
    private String stagingConnString;

    @Value("${azure.storage-account.export.connection-string}")
    private String exportConnString;

    @Autowired
    private BlobServiceClientFactory blobServiceClientFactory;

    public BlobServiceClient getStagingClient() {
        if (StringUtils.isEmpty(clientId)) {
            log.info("Getting staging blob service client by connection string.");
            return blobServiceClientFactory.getBlobClientWithConnectionString(stagingConnString);
        } else {
            log.info("Getting staging blob service client by managed identity.");
            return blobServiceClientFactory.getBlobClientWithManagedIdentity(clientId, stagingName);
        }
    }

    public BlobServiceClient getExportClient() {
        if (StringUtils.isEmpty(clientId)) {
            log.info("Getting export blob service client by connection string.");
            return blobServiceClientFactory.getBlobClientWithConnectionString(exportConnString);
        } else {
            log.info("Getting export blob service client by managed identity.");
            return blobServiceClientFactory.getBlobClientWithManagedIdentity(clientId, exportName);
        }
    }
}
