package uk.gov.hmcts.reform.mi.miextractionservice.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class GovUkNotifyClientTest {

    @Test
    void givenApiKey_whenSetupClient_thenReturnClientWithApiKey() {
        String testApiKey = "testApiKey";

        GovUkNotifyClient underTest = new GovUkNotifyClient(testApiKey);

        assertEquals(testApiKey, underTest.getApiKey(), "Expected test api key to be set in client under test.");
    }
}
