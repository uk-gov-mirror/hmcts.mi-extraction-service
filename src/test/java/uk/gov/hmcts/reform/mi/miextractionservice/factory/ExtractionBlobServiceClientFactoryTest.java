package uk.gov.hmcts.reform.mi.miextractionservice.factory;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.micore.factory.BlobServiceClientFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ExtractionBlobServiceClientFactoryTest {

    private static final String TEST_CLIENT_ID = "testId";
    private static final String TEST_STAGING_NAME = "persistentStore";
    private static final String TEST_EXPORT_NAME = "exportStore";
    private static final String TEST_STAGING_CONN_STRING = "testStaging";
    private static final String TEST_EXPORT_CONN_STRING = "testExport";

    @Mock
    private BlobServiceClientFactory blobServiceClientFactory;

    @InjectMocks
    private ExtractionBlobServiceClientFactory underTest;

    private BlobServiceClient blobServiceClient;

    @BeforeEach
    public void setUp() {
        blobServiceClient = mock(BlobServiceClient.class);
    }

    @Test
    public void givenManagedIdentity_whenGetStagingBlobServiceClient_returnStagingBlobServiceClient() {
        ReflectionTestUtils.setField(underTest, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(underTest, "stagingName", TEST_STAGING_NAME);

        when(blobServiceClientFactory.getBlobClientWithManagedIdentity(TEST_CLIENT_ID, TEST_STAGING_NAME)).thenReturn(blobServiceClient);

        assertEquals(blobServiceClient, underTest.getStagingClient(), "Managed Identity staging blob client was not retrieved.");
    }

    @Test
    public void givenManagedIdentity_whenGetExportBlobServiceClient_returnExportBlobServiceClient() {
        ReflectionTestUtils.setField(underTest, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(underTest, "exportName", TEST_EXPORT_NAME);

        when(blobServiceClientFactory.getBlobClientWithManagedIdentity(TEST_CLIENT_ID, TEST_EXPORT_NAME)).thenReturn(blobServiceClient);

        assertEquals(blobServiceClient, underTest.getExportClient(), "Managed Identity export blob client was not retrieved.");
    }

    @Test
    public void givenConnectionString_whenGetStagingBlobServiceClient_returnStagingBlobServiceClient() {
        ReflectionTestUtils.setField(underTest, "stagingConnString", TEST_STAGING_CONN_STRING);

        when(blobServiceClientFactory.getBlobClientWithConnectionString(TEST_STAGING_CONN_STRING)).thenReturn(blobServiceClient);

        assertEquals(blobServiceClient, underTest.getStagingClient(), "Connection string staging blob client was not retrieved.");
    }

    @Test
    public void givenConnectionString_whenGetExportBlobServiceClient_returnExportBlobServiceClient() {
        ReflectionTestUtils.setField(underTest, "exportConnString", TEST_EXPORT_CONN_STRING);

        when(blobServiceClientFactory.getBlobClientWithConnectionString(TEST_EXPORT_CONN_STRING)).thenReturn(blobServiceClient);

        assertEquals(blobServiceClient, underTest.getExportClient(), "Connection string export blob client was not retrieved.");
    }
}
