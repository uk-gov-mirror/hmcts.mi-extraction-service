package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClientException;

import uk.gov.hmcts.reform.mi.miextractionservice.client.GovUkNotifyClient;
import uk.gov.hmcts.reform.mi.miextractionservice.component.SendEmailComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_BLOB_URL_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_EMAIL_REFERENCE;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.TIME_TO_EXPIRY;

@Primary
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${mail.gov-uk.api-key}')")
@Component
public class SendBlobUrlNotifyEmailComponentImpl implements SendEmailComponent {

    @Value("${mail.gov.uk.templates.blob-url}")
    private String blobUrlTemplateId;

    @Autowired
    private GovUkNotifyClient govUkNotifyClient;

    @Override
    public void sendEmail(String emailAddress, String subject, String content) {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put(NOTIFY_BLOB_URL_TEMPLATE_PARAMETER, content);
        parameters.put(NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER, String.valueOf(TIME_TO_EXPIRY).concat(" hours"));

        try {
            govUkNotifyClient.sendEmail(blobUrlTemplateId, emailAddress, parameters, NOTIFY_EMAIL_REFERENCE);
        } catch (NotificationClientException e) {
            throw new ExportException("Unable to send email via Gov UK notify.", e);
        }
    }
}
