package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.GovUkNotifyComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.SendEmailComponent;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.mi.miextractionservice.model.MiExtractionServiceConstants.NOTIFY_BLOB_URL_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.model.MiExtractionServiceConstants.NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER;
import static uk.gov.hmcts.reform.mi.miextractionservice.model.MiExtractionServiceConstants.TIME_TO_EXPIRY;

@Primary
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${mail.gov-uk.api-key}')")
@Component
public class SendGovUkNotifyEmailComponentImpl implements SendEmailComponent {

    @Value("${mail.gov.uk.templates.blob-url}")
    private String blobUrlTemplateId;

    @Autowired
    private GovUkNotifyComponent govUkNotifyComponent;

    @Override
    public void sendEmail(String emailAddress, String subject, String content) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(NOTIFY_BLOB_URL_TEMPLATE_PARAMETER, content);
        parameters.put(NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER, String.valueOf(TIME_TO_EXPIRY).concat(" hours"));

        govUkNotifyComponent.sendNotifyEmail(blobUrlTemplateId, emailAddress, parameters);
    }
}
