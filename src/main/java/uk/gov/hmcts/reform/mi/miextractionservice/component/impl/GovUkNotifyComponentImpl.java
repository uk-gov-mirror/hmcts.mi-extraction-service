package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import uk.gov.hmcts.reform.mi.miextractionservice.component.GovUkNotifyComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.util.Map;

@Component
public class GovUkNotifyComponentImpl implements GovUkNotifyComponent {

    @Value("${mail.gov-uk.api-key}")
    private String notifyApiKey;

    @Override
    public void sendNotifyEmail(String email, String templateId, Map<String, Object> parameters) {
        NotificationClient client = new NotificationClient(notifyApiKey);

        try {
            client.sendEmail(templateId, email, parameters, null, null);
        } catch (NotificationClientException exception) {
            throw new ExportException("Unable to send email via Gov UK notify.", exception);
        }
    }
}
