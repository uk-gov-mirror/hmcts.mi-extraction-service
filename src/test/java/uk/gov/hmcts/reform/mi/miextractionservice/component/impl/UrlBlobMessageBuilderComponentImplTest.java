package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UrlBlobMessageBuilderComponentImplTest {

    private static final String AZURE_PORTAL_URL = "https://portal.azure.com";
    private static final String BODY = "Please visit %s. The output can be found in the <%s> storage account, <%s> container with the name <%s>";

    private static final String TEST_ACCOUNT_NAME = "testAccountName";
    private static final String TEST_CONTAINER_NAME = "testContainerName";
    private static final String TEST_BLOB_NAME = "testBlobName";

    @InjectMocks
    private UrlBlobMessageBuilderComponentImpl underTest;

    @Test
    void givenNoWhitelistedIps_whenBuildMessage_thenReturnMessageWithUrl() {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);

        when(blobServiceClient.getAccountName()).thenReturn(TEST_ACCOUNT_NAME);

        String expectedBody = String.format(BODY, AZURE_PORTAL_URL, TEST_ACCOUNT_NAME, TEST_CONTAINER_NAME, TEST_BLOB_NAME);

        assertEquals(expectedBody, underTest.buildMessage(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME),
            "Actual message does not match expected message.");
    }
}
