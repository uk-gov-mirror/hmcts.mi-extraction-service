package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.SendEmailComponent;

@Component
public class EmailBlobUrlToTargetsComponentImpl implements EmailBlobUrlToTargetsComponent {

    private static final String DELIMITER = ",";
    private static final String EMAIL_SUBJECT = "Management Information Exported Data Url";

    @Value("${mail.targets}")
    private String targets;

    @Autowired
    private SendEmailComponent sendEmailComponent;

    @SuppressWarnings("PMD.LawOfDemeter")
    @Override
    public void sendBlobUrl(String url) {
        if (Boolean.FALSE.equals(StringUtils.isEmpty(targets))) {
            // Assume list of targets is comma separated string.
            String[] recipients = targets.split(DELIMITER);

            for (String toEmail : recipients) {
                sendEmailComponent.sendEmail(toEmail, EMAIL_SUBJECT, url);
            }
        }
    }
}
