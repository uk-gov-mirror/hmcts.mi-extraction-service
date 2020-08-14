package uk.gov.hmcts.reform.mi.miextractionservice.factory.azure;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExtractionBlobServiceClientFactoryTest {

    private static final String TEST_CLIENT_ID = "testId";
    private static final String TEST_STAGING_NAME = "persistentStore";
    private static final String TEST_EXPORT_NAME = "exportStore";
    private static final String TEST_STAGING_CONN_STRING = "testStaging";
    private static final String TEST_EXPORT_CONN_STRING = "testExport";

    @Mock private BlobServiceClientFactory blobServiceClientFactory;

    private ExtractionBlobServiceClientFactory classToTest;

    @Test
    void givenManagedIdentity_whenGetStagingBlobServiceClient_returnStagingBlobServiceClient() {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        when(blobServiceClientFactory.getBlobClientWithManagedIdentity(TEST_CLIENT_ID, TEST_STAGING_NAME))
            .thenReturn(blobServiceClient);

        classToTest = new ExtractionBlobServiceClientFactory(TEST_CLIENT_ID, TEST_STAGING_NAME, null,
                                                           null, null, blobServiceClientFactory);

        BlobServiceClient actual = classToTest.getStagingClient();

        assertEquals(blobServiceClient, actual, "Should return staging BlobServiceClient for Managed Identity.");
    }

    @Test
    void givenManagedIdentity_whenGetExportBlobServiceClient_returnExportBlobServiceClient() {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        when(blobServiceClientFactory.getBlobClientWithManagedIdentity(TEST_CLIENT_ID, TEST_EXPORT_NAME))
            .thenReturn(blobServiceClient);

        classToTest = new ExtractionBlobServiceClientFactory(TEST_CLIENT_ID, null, TEST_EXPORT_NAME,
                                                           null, null, blobServiceClientFactory);

        BlobServiceClient actual = classToTest.getExportClient();

        assertEquals(blobServiceClient, actual, "Should return export BlobServiceClient for Managed Identity.");
    }

    @Test
    void givenConnectionString_whenGetStagingBlobServiceClient_returnStagingBlobServiceClient() {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        when(blobServiceClientFactory.getBlobClientWithConnectionString(TEST_STAGING_CONN_STRING))
            .thenReturn(blobServiceClient);

        classToTest = new ExtractionBlobServiceClientFactory(null, null, null,
                                                           TEST_STAGING_CONN_STRING, null, blobServiceClientFactory);

        BlobServiceClient actual = classToTest.getStagingClient();

        assertEquals(blobServiceClient, actual, "Should return staging BlobServiceClient for connection string.");
    }

    @Test
    void givenConnectionString_whenGetExportBlobServiceClient_returnExportBlobServiceClient() {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        when(blobServiceClientFactory.getBlobClientWithConnectionString(TEST_EXPORT_CONN_STRING))
            .thenReturn(blobServiceClient);

        classToTest = new ExtractionBlobServiceClientFactory(null, null, null,
                                                           null, TEST_EXPORT_CONN_STRING, blobServiceClientFactory);

        BlobServiceClient actual = classToTest.getExportClient();

        assertEquals(blobServiceClient, actual, "Should return export BlobServiceClient for connection string.");
    }
}
