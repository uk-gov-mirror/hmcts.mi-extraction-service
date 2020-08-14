package uk.gov.hmcts.reform.mi.miextractionservice.component.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.service.notify.NotificationClientException;

import uk.gov.hmcts.reform.mi.miextractionservice.client.email.GovUkNotifyClient;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.EMAIL_TIME_TO_EXPIRY;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NOTIFY_BLOB_URL_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NOTIFY_EMAIL_REFERENCE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER;

@ExtendWith(SpringExtension.class)
class SendEmailComponentImplTest {

    private static final String TEST_EMAIL = "testEmail@test";
    private static final String TEST_CONTENT = "Test content with blob url https://hello.world";
    private static final String TEST_TEMPLATE_ID = "testTemplateId";

    @Mock private GovUkNotifyClient govUkNotifyClient;

    private SendEmailComponentImpl classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new SendEmailComponentImpl(TEST_TEMPLATE_ID, govUkNotifyClient);
    }

    @Test
    void givenEmailAndContent_whenSendEmailViaNotify_thenCallToClientShouldBeMade() throws Exception {
        classToTest.sendEmail(TEST_EMAIL, "NotRequired", TEST_CONTENT);

        Map<String, Object> expectedMap = new ConcurrentHashMap<>();
        expectedMap.put(NOTIFY_BLOB_URL_TEMPLATE_PARAMETER, TEST_CONTENT);
        expectedMap.put(NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER, EMAIL_TIME_TO_EXPIRY + " hours");

        verify(govUkNotifyClient, times(1))
            .sendEmail(eq(TEST_TEMPLATE_ID), eq(TEST_EMAIL), eq(expectedMap), eq(NOTIFY_EMAIL_REFERENCE));
    }

    @Test
    void givenErrorInNotifyClient_whenSendEmailViaNotify_thenThrowExportException() throws Exception {
        doThrow(new NotificationClientException("Error"))
            .when(govUkNotifyClient).sendEmail(any(), any(), any(), any());

        assertThrows(ExportException.class, () -> classToTest.sendEmail(TEST_EMAIL, "NotRequired", TEST_CONTENT));
    }
}
