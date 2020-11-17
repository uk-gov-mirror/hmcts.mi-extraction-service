package uk.gov.hmcts.reform.mi.miextractionservice.component.notification;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.COMMA_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.EMAIL_SUBJECT;

@AllArgsConstructor
@Component
public class NotifyTargetsComponentImpl implements NotifyTargetsComponent {

    private final @Value("${mail.targets}") String targets;
    private final SendEmailComponent sendEmailComponent;

    @Override
    public void sendMessage(String message) {
        if (StringUtils.isNotEmpty(targets)) {
            // Assume list of targets is comma separated string.
            String[] recipients = targets.split(COMMA_DELIMITER);

            for (String toEmail : recipients) {
                sendEmailComponent.sendEmail(toEmail, EMAIL_SUBJECT, message);
            }
        }
    }
}
