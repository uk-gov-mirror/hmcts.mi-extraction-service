package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class SendEmailComponentImplTest {

    private static final String TEST_EMAIL = "testEmail@example.com";
    private static final String TEST_SUBJECT = "testSubject";
    private static final String TEST_CONTENT = "testContent";

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SendEmailComponentImpl underTest;

    @Test
    void givenEmailSubjectAndContent_whenSendEmail_thenJavaMailSenderIsTriggered() {
        underTest.sendEmail(TEST_EMAIL, TEST_SUBJECT, TEST_CONTENT);

        SimpleMailMessage expectedMailMessage = new SimpleMailMessage();
        expectedMailMessage.setTo(TEST_EMAIL);
        expectedMailMessage.setSubject(TEST_SUBJECT);
        expectedMailMessage.setText(TEST_CONTENT);

        verify(javaMailSender, times(1)).send(refEq(expectedMailMessage));
    }
}
