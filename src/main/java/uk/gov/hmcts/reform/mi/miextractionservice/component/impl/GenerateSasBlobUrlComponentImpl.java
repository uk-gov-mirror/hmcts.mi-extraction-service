package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

@Component
public class GenerateSasBlobUrlComponentImpl implements GenerateBlobUrlComponent {

    private static final long TIME_TO_EXPIRY = 24L; // In hours.
    private static final String QUERY_PART_DELIMITER = "?";

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Override
    public String generateUrlForBlob(BlobServiceClient blobServiceClient, String containerName, String blobName) {
        BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues blobServiceSasSignatureValues =
            new BlobServiceSasSignatureValues(dateTimeUtil.getCurrentDateTime().plusHours(TIME_TO_EXPIRY), blobSasPermission);

        BlobClient blobClient = blobServiceClient
            .getBlobContainerClient(containerName)
            .getBlobClient(blobName);

        return String.join(QUERY_PART_DELIMITER, blobClient.getBlobUrl(), blobClient.generateSas(blobServiceSasSignatureValues));
    }
}
