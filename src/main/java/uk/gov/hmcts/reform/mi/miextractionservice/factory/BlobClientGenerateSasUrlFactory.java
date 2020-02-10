package uk.gov.hmcts.reform.mi.miextractionservice.factory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.TIME_TO_EXPIRY;

@Slf4j
@Component
public class BlobClientGenerateSasUrlFactory {

    @Value("${azure.managed-identity.client-id}")
    private String clientId;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @SuppressWarnings("PMD.LawOfDemeter")
    public String getSasUrl(BlobServiceClient blobServiceClient, String containerName, String blobName,
                            BlobServiceSasSignatureValues blobServiceSasSignatureValues) {

        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);

        if (Boolean.FALSE.equals(StringUtils.isEmpty(clientId))) {
            log.info("Getting SAS url with user delegation.");
            UserDelegationKey userDelegationKey = blobServiceClient
                .getUserDelegationKey(dateTimeUtil.getCurrentDateTime(), dateTimeUtil.getCurrentDateTime().plusHours(TIME_TO_EXPIRY));

            return blobClient.generateUserDelegationSas(blobServiceSasSignatureValues, userDelegationKey);
        } else {
            log.info("Getting SAS url with service credentials.");
            return blobClient.generateSas(blobServiceSasSignatureValues);
        }
    }
}
