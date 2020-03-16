package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UrlBlobMessageBuilderComponentImplTest {

    private static final String TEST_CONTAINER_NAME = "testContainerName";
    private static final String TEST_BLOB_NAME = "testBlobName";
    private static final String TEST_BLOB_URL = "testBlobUrl";

    @InjectMocks
    private UrlBlobMessageBuilderComponentImpl underTest;

    @Test
    public void givenNoWhitelistedIps_whenBuildMessage_thenReturnMessageWithUrl() {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);

        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(TEST_BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn(TEST_BLOB_URL);

        assertEquals(TEST_BLOB_URL, underTest.buildMessage(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME),
            "Actual message does not match expected message.");
    }
}
