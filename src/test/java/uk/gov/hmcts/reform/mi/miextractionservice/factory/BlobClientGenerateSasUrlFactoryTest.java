package uk.gov.hmcts.reform.mi.miextractionservice.factory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class BlobClientGenerateSasUrlFactoryTest {

    private static final OffsetDateTime MOCK_CURRENT_DATETIME = OffsetDateTime.MIN;
    private static final long TIME_TO_EXPIRY = 24L;

    private static final String TEST_QUERY_PARAMS = "testQuery=testParam&anotherTestQuery=value";

    @Mock
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private BlobClientGenerateSasUrlFactory underTest;

    @Test
    public void givenUsingManagedIdentity_whenGetSasUrl_thenReturnSasUrlWithUserDelegationCall() {
        ReflectionTestUtils.setField(underTest, "clientId", "existingId");

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

        UserDelegationKey userDelegationKey = mock(UserDelegationKey.class);
        when(blobServiceClient.getUserDelegationKey(MOCK_CURRENT_DATETIME, MOCK_CURRENT_DATETIME.plusHours(TIME_TO_EXPIRY)))
            .thenReturn(userDelegationKey);

        BlobServiceSasSignatureValues blobServiceSasSignatureValues = new BlobServiceSasSignatureValues(
            OffsetDateTime.MIN, new BlobSasPermission().setReadPermission(true)
        );

        when(mockBlobClient.generateUserDelegationSas(eq(blobServiceSasSignatureValues), eq(userDelegationKey)))
            .thenReturn(TEST_QUERY_PARAMS);

        assertEquals(TEST_QUERY_PARAMS, underTest.getSasUrl(blobServiceClient, containerName, blobName, blobServiceSasSignatureValues),
            "Result string should match output of getSasUrl function when using managed identity.");

        verify(mockBlobClient, never()).generateSas(any());
    }

    @Test
    public void givenNotUsingManagedIdentity_whenGetSasUrl_thenReturnSasUrlWithNormalSasCall() {
        ReflectionTestUtils.setField(underTest, "clientId", "");

        // Given
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        String containerName = "test-container";
        String blobName = "test-blob";

        // Mock Azure Logic
        BlobContainerClient mockBlobContainerClient = mock(BlobContainerClient.class);
        BlobClient mockBlobClient = mock(BlobClient.class);

        when(blobServiceClient.getBlobContainerClient(containerName)).thenReturn(mockBlobContainerClient);
        when(mockBlobContainerClient.getBlobClient(blobName)).thenReturn(mockBlobClient);

        BlobServiceSasSignatureValues blobServiceSasSignatureValues = new BlobServiceSasSignatureValues(
            OffsetDateTime.MIN, new BlobSasPermission().setReadPermission(true)
        );

        when(mockBlobClient.generateSas(eq(blobServiceSasSignatureValues))).thenReturn(TEST_QUERY_PARAMS);

        assertEquals(TEST_QUERY_PARAMS, underTest.getSasUrl(blobServiceClient, containerName, blobName, blobServiceSasSignatureValues),
            "Result string should match output of getSasUrl function without managed identity.");

        verify(mockBlobClient, never()).generateUserDelegationSas(any(), any());
    }
}
