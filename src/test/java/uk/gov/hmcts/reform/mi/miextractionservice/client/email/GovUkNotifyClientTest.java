package uk.gov.hmcts.reform.mi.miextractionservice.client.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class GovUkNotifyClientTest {

    private static final String TEST_API_KEY = "testApiKey";

    private GovUkNotifyClient classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new GovUkNotifyClient(TEST_API_KEY);
    }

    @Test
    void givenApiKey_whenSetupClient_thenReturnClientWithApiKey() {
        String actualApiKey = classToTest.getApiKey();

        assertEquals(TEST_API_KEY, actualApiKey,
                     "Client API key should match setup key.");
    }
}
