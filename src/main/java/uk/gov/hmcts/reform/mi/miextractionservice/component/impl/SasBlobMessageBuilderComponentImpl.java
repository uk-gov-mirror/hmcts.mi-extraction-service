package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.BlobMessageBuilderComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SasIpWhitelist;

@Component
@ConditionalOnProperty(prefix = "mail.sas", name = "enabled", havingValue = "true")
public class SasBlobMessageBuilderComponentImpl implements BlobMessageBuilderComponent {

    private static final String MESSAGE_DELIMITER = " : ";
    private static final String MESSAGE_NEWLINE_DELIMITER = "\n\n";
    private static final String LOCATION_DELIMITER = "-";
    private static final String DOT_DELIMITER = ".";
    private static final String SPACE_DELIMITER = " ";

    @Autowired
    private SasIpWhitelist sasIpWhitelist;

    @Autowired
    private GenerateBlobUrlComponent generateBlobUrlComponent;

    @Override
    public String buildMessage(BlobServiceClient blobServiceClient, String containerName, String blobName) {
        String message = "";

        if (sasIpWhitelist.getRange().isEmpty()) {
            message = generateBlobUrlComponent.generateUrlForBlob(blobServiceClient, containerName, blobName);
        } else {
            message = "Each link is restricted by IP address for security. Please check you use the correct one." + MESSAGE_NEWLINE_DELIMITER;

            for (String key : sasIpWhitelist.getRange().keySet()) {
                String locationName = key
                    .replace(LOCATION_DELIMITER, SPACE_DELIMITER)
                    .replace(DOT_DELIMITER, SPACE_DELIMITER);

                locationName = WordUtils.capitalizeFully(locationName);

                message = message.concat(locationName + MESSAGE_DELIMITER + generateBlobUrlComponent
                    .generateUrlForBlobWithIpRange(blobServiceClient, containerName, blobName, sasIpWhitelist.getRange().get(key)));

                message = message.concat(MESSAGE_NEWLINE_DELIMITER);
            }
        }

        return message;
    }
}
