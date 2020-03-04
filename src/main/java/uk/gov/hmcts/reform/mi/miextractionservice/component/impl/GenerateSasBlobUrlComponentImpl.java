package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasIpRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.factory.BlobClientGenerateSasUrlFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.QUERY_PART_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.TIME_TO_EXPIRY;

@Component
public class GenerateSasBlobUrlComponentImpl implements GenerateBlobUrlComponent {

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Autowired
    private BlobClientGenerateSasUrlFactory blobClientGenerateSasUrlFactory;

    @Override
    public String generateUrlForBlob(BlobServiceClient blobServiceClient, String containerName, String blobName) {
        return generateUrlForBlobWithIpRange(blobServiceClient, containerName, blobName, null);
    }

    @Override
    public String generateUrlForBlobWithIpRange(BlobServiceClient blobServiceClient, String containerName, String blobName, String ipRange) {
        BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues blobServiceSasSignatureValues =
            new BlobServiceSasSignatureValues(dateTimeUtil.getCurrentDateTime().plusHours(TIME_TO_EXPIRY), blobSasPermission);

        if (ipRange != null) {
            blobServiceSasSignatureValues.setSasIpRange(SasIpRange.parse(ipRange));
        }

        BlobClient blobClient = blobServiceClient
            .getBlobContainerClient(containerName)
            .getBlobClient(blobName);

        return String.join(
            QUERY_PART_DELIMITER,
            blobClient.getBlobUrl(),
            blobClientGenerateSasUrlFactory.getSasUrl(blobServiceClient, containerName, blobName, blobServiceSasSignatureValues)
        );
    }
}
