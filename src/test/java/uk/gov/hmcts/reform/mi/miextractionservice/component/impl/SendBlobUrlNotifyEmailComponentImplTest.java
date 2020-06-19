package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.service.notify.NotificationClientException;

import uk.gov.hmcts.reform.mi.miextractionservice.client.GovUkNotifyClient;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_BLOB_URL_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_EMAIL_REFERENCE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.TIME_TO_EXPIRY;

@ExtendWith(SpringExtension.class)
class SendBlobUrlNotifyEmailComponentImplTest {

    private static final String TEST_EMAIL = "testEmail@test";
    private static final String TEST_CONTENT = "Test content with blob url https://hello.world";
    private static final String BLOB_URL_TEMPLATE_ID = "testId";

    @Mock
    private GovUkNotifyClient govUkNotifyClient;

    @InjectMocks
    private SendBlobUrlNotifyEmailComponentImpl underTest;

    @Test
    void givenEmailAndContent_whenSendEmailViaNotify_thenCallToClientShouldBeMade() throws Exception {
        ReflectionTestUtils.setField(underTest, "blobUrlTemplateId", BLOB_URL_TEMPLATE_ID);

        underTest.sendEmail(TEST_EMAIL, "NotRequired", TEST_CONTENT);

        Map<String, Object> expectedMap = new ConcurrentHashMap<>();
        expectedMap.put(NOTIFY_BLOB_URL_TEMPLATE_PARAMETER, TEST_CONTENT);
        expectedMap.put(NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER, TIME_TO_EXPIRY + " hours");

        verify(govUkNotifyClient).sendEmail(eq(BLOB_URL_TEMPLATE_ID), eq(TEST_EMAIL), eq(expectedMap), eq(NOTIFY_EMAIL_REFERENCE));
    }

    @Test
    void givenErrorInNotifyClient_whenSendEmailViaNotify_thenThrowExportException() throws Exception {
        doThrow(new NotificationClientException("Error")).when(govUkNotifyClient).sendEmail(any(), any(), any(), any());

        assertThrows(ExportException.class, () -> underTest.sendEmail(TEST_EMAIL, "NotRequired", TEST_CONTENT));
    }
}
