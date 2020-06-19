package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BlobDownloadComponentImplTest {

    private static final String TEST_CONTAINER_NAME = "testContainer";
    private static final String TEST_BLOB_NAME = "testBlob";

    private BlobDownloadComponentImpl underTest;

    private BlobServiceClient blobServiceClient;

    @BeforeEach
    void setUp() {
        underTest = new BlobDownloadComponentImpl();

        blobServiceClient = mock(BlobServiceClient.class);
    }

    @SuppressWarnings({"PMD.CloseResource","PMD.JUnitAssertionsShouldIncludeMessage"})
    @Test
    void givenBlobServiceClientAndContainerNameAndBlobName_whenOpenBlobInputStream_thenReturnInputStream() {
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER_NAME)).thenReturn(blobContainerClient);

        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(TEST_BLOB_NAME)).thenReturn(blobClient);

        BlobInputStream mockBlobInputStream = mock(BlobInputStream.class);
        when(blobClient.openInputStream()).thenReturn(mockBlobInputStream);

        assertEquals(mockBlobInputStream, underTest.openBlobInputStream(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME),
            "Expected returned inputStream to be same as mocked stream.");
    }
}
