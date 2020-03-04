package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasIpRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.factory.BlobClientGenerateSasUrlFactory;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.CustomMatchers.deepRefEq;

@ExtendWith(SpringExtension.class)
public class GenerateSasBlobUrlComponentImplTest {

    private static final OffsetDateTime MOCK_CURRENT_DATETIME = OffsetDateTime.MIN;
    private static final long TIME_TO_EXPIRY = 24L;

    private static final String TEST_BLOB_URL = "https://testBlob.url";
    private static final String TEST_QUERY_PARAMS = "testQuery=testParam&anotherTestQuery=value";

    private static final String LOGGER_PROPERTY = "logger";

    @Mock
    private DateTimeUtil dateTimeUtil;

    @Mock
    private BlobClientGenerateSasUrlFactory blobClientGenerateSasUrlFactory;

    @InjectMocks
    private GenerateSasBlobUrlComponentImpl underTest;

    @Test
    public void givenBlobServiceClientAndContainerNameAndBlob_whenGenerateSas_thenReturnAssociatedLink() {
        // Given
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        String containerName = "test-container";
        String blobName = "test-blob";

        // Mock Azure Logic
        BlobContainerClient mockBlobContainerClient = mock(BlobContainerClient.class);
        BlobClient mockBlobClient = mock(BlobClient.class);

        when(blobServiceClient.getBlobContainerClient(containerName)).thenReturn(mockBlobContainerClient);
        when(mockBlobContainerClient.getBlobClient(blobName)).thenReturn(mockBlobClient);

        when(dateTimeUtil.getCurrentDateTime()).thenReturn(MOCK_CURRENT_DATETIME);

        when(mockBlobClient.getBlobUrl()).thenReturn(TEST_BLOB_URL);

        BlobServiceSasSignatureValues blobServiceSasSignatureValues = new BlobServiceSasSignatureValues(
            dateTimeUtil.getCurrentDateTime().plusHours(TIME_TO_EXPIRY), new BlobSasPermission().setReadPermission(true)
        );

        when(blobClientGenerateSasUrlFactory
            .getSasUrl(eq(blobServiceClient), eq(containerName), eq(blobName), deepRefEq(blobServiceSasSignatureValues, LOGGER_PROPERTY)))
            .thenReturn(TEST_QUERY_PARAMS);

        // When
        String sasUrl = underTest.generateUrlForBlob(blobServiceClient, containerName, blobName);

        // Then
        assertEquals(TEST_BLOB_URL + "?" + TEST_QUERY_PARAMS, sasUrl, "Generated url does not match expected.");
    }

    @Test
    public void givenBlobServiceClientAndContainerNameAndBlobWithIpRange_whenGenerateSas_thenReturnAssociatedLink() {
        // Given
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        String containerName = "test-container";
        String blobName = "test-blob";

        // Mock Azure Logic
        BlobContainerClient mockBlobContainerClient = mock(BlobContainerClient.class);
        BlobClient mockBlobClient = mock(BlobClient.class);

        when(blobServiceClient.getBlobContainerClient(containerName)).thenReturn(mockBlobContainerClient);
        when(mockBlobContainerClient.getBlobClient(blobName)).thenReturn(mockBlobClient);

        when(dateTimeUtil.getCurrentDateTime()).thenReturn(MOCK_CURRENT_DATETIME);

        when(mockBlobClient.getBlobUrl()).thenReturn(TEST_BLOB_URL);

        BlobServiceSasSignatureValues blobServiceSasSignatureValues = new BlobServiceSasSignatureValues(
            dateTimeUtil.getCurrentDateTime().plusHours(TIME_TO_EXPIRY), new BlobSasPermission().setReadPermission(true)
        );

        String ipRange = "100.0.0.100-200.0.0.200";

        blobServiceSasSignatureValues.setSasIpRange(SasIpRange.parse(ipRange));

        when(blobClientGenerateSasUrlFactory
            .getSasUrl(eq(blobServiceClient), eq(containerName), eq(blobName), deepRefEq(blobServiceSasSignatureValues, LOGGER_PROPERTY)))
            .thenReturn(TEST_QUERY_PARAMS);

        // When
        String sasUrl = underTest.generateUrlForBlobWithIpRange(blobServiceClient, containerName, blobName, ipRange);

        // Then
        assertEquals(TEST_BLOB_URL + "?" + TEST_QUERY_PARAMS, sasUrl, "Generated url does not match expected.");
    }
}
