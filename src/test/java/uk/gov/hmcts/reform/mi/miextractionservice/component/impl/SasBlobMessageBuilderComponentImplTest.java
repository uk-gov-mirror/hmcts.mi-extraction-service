package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.azure.storage.blob.BlobServiceClient;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.GenerateBlobUrlComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SasIpWhitelist;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SasBlobMessageBuilderComponentImplTest {

    private static final String TEST_CONTAINER_NAME = "testContainerName";
    private static final String TEST_BLOB_NAME = "testBlobName";
    private static final String TEST_BLOB_URL = "testBlobUrl";

    private static final String ENCRYPTION_ENABLED_KEY = "encryptionEnabled";

    @Mock
    private SasIpWhitelist sasIpWhitelist;

    @Mock
    private GenerateBlobUrlComponent generateBlobUrlComponent;

    @InjectMocks
    private SasBlobMessageBuilderComponentImpl underTest;

    @Test
    void givenNoWhitelistedIps_whenBuildMessage_thenReturnMessageWithUrl() {
        ReflectionTestUtils.setField(underTest, ENCRYPTION_ENABLED_KEY, "true");

        when(sasIpWhitelist.getRange()).thenReturn(Collections.emptyMap());

        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);

        when(generateBlobUrlComponent.generateUrlForBlob(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME))
            .thenReturn(TEST_BLOB_URL);

        assertEquals(TEST_BLOB_URL, underTest.buildMessage(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME),
            "Actual message does not match expected message.");
    }

    @Test
    void givenListOfWhitelistedIps_whenBuildMessage_thenReturnMessageWitMultipleBlobUrls() {
        ReflectionTestUtils.setField(underTest, ENCRYPTION_ENABLED_KEY, "true");

        String homeOffice = "home.office";
        String homeIp = "homeIp";
        String workOffice = "work-office";
        String workIp = "workIp";

        when(sasIpWhitelist.getRange()).thenReturn(ImmutableMap.of(
            homeOffice, homeIp,
            workOffice, workIp
        ));

        String workBlobUrl = "testWorkBlob";

        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);

        when(generateBlobUrlComponent.generateUrlForBlobWithIpRange(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME, homeIp))
            .thenReturn(TEST_BLOB_URL);
        when(generateBlobUrlComponent.generateUrlForBlobWithIpRange(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME, workIp))
            .thenReturn(workBlobUrl);

        String expectedMessage = "Each link is restricted by IP address for security. Please check you use the correct one."
            + "\n\n"
            + "Home Office : " + TEST_BLOB_URL
            + "\n\n"
            + "Work Office : " + workBlobUrl
            + "\n\n";

        assertEquals(expectedMessage, underTest.buildMessage(blobServiceClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME),
            "Actual message does not match expected message.");
    }

    @Test
    void givenNoEncryptionSet_whenBuildMessage_thenThrowExportException() {
        ReflectionTestUtils.setField(underTest, ENCRYPTION_ENABLED_KEY, "false");
        BlobServiceClient mockClient = mock(BlobServiceClient.class);
        assertThrows(ExportException.class, () -> underTest.buildMessage(mockClient, TEST_CONTAINER_NAME, TEST_BLOB_NAME));
    }
}
