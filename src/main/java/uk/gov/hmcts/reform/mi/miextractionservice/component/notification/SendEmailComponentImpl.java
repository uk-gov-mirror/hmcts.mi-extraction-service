package uk.gov.hmcts.reform.mi.miextractionservice.component.notification;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClientException;

import uk.gov.hmcts.reform.mi.miextractionservice.client.email.GovUkNotifyClient;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.EMAIL_TIME_TO_EXPIRY;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NOTIFY_BLOB_URL_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NOTIFY_EMAIL_REFERENCE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER;

@AllArgsConstructor
@Component
public class SendEmailComponentImpl implements SendEmailComponent {

    private final @Value("${mail.gov-uk.templates.blob-url}") String templateId;
    private final GovUkNotifyClient govUkNotifyClient;

    @Override
    public void sendEmail(String emailAddress, String subject, String content) {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put(NOTIFY_BLOB_URL_TEMPLATE_PARAMETER, content);
        parameters.put(NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER, String.valueOf(EMAIL_TIME_TO_EXPIRY).concat(" hours"));

        try {
            govUkNotifyClient.sendEmail(templateId, emailAddress, parameters, NOTIFY_EMAIL_REFERENCE);
        } catch (NotificationClientException e) {
            throw new ExportException("Unable to send email via Gov UK notify.", e);
        }
    }
}
