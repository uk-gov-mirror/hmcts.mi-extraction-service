package uk.gov.hmcts.reform.mi.miextractionservice.component.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.COMMA_DELIMITER;

@ExtendWith(SpringExtension.class)
class NotifyTargetsComponentImplTest {

    private static final String TARGET_ONE = "targetOne";
    private static final String TARGET_TWO = "targetTwo";
    private static final String MAIL_TARGETS = TARGET_ONE + COMMA_DELIMITER + TARGET_TWO;
    private static final String TEST_MESSAGE = "This is a test message.";

    @Mock private SendEmailComponent sendEmailComponent;

    private NotifyTargetsComponentImpl classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new NotifyTargetsComponentImpl(MAIL_TARGETS, sendEmailComponent);
    }

    @Test
    void givenTargetsAndMessage_whenEmailToTargets_thenCallEmailMethodForEachRecipient() {
        classToTest.sendMessage(TEST_MESSAGE);

        verify(sendEmailComponent, times(1))
            .sendEmail(eq(TARGET_ONE), anyString(), eq(TEST_MESSAGE));
        verify(sendEmailComponent, times(1))
            .sendEmail(eq(TARGET_TWO), anyString(), eq(TEST_MESSAGE));
    }

    @Test
    void givenNoTargets_whenEmailBlobUrlToTargets_thenDoNothing() {
        classToTest = new NotifyTargetsComponentImpl("", sendEmailComponent);
        classToTest.sendMessage(TEST_MESSAGE);

        verify(sendEmailComponent, never())
            .sendEmail(anyString(), anyString(), anyString());
    }
}
