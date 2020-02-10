package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.GovUkNotifyComponent;

@ExtendWith(SpringExtension.class)
public class GovUkNotifyComponentImplTest {

    private static final String TEST_KEY = "testKey";

    private GovUkNotifyComponentImpl underTest;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(underTest, "notifyApiKey", );
    }
}
