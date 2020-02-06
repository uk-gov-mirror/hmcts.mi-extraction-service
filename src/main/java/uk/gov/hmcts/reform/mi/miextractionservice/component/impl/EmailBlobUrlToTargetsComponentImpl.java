package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EmailBlobUrlToTargetsComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.SendEmailComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

@Component
public class EmailBlobUrlToTargetsComponentImpl implements EmailBlobUrlToTargetsComponent {

    private static final String DELIMITER = ",";
    private static final String EMAIL_SUBJECT = "Management Information Exported Data Url";

    @Value("${mail.targets}")
    private String targets;

    @Autowired
    private SendEmailComponent sendEmailComponent;

    @Override
    public void sendBlobUrl(String url) {
        if (StringUtils.isEmpty(targets)) {
            throw new ExportException("No target emails defined.");
        }

        // Assume list of targets is comma separated string.
        String[] recipients = targets.split(DELIMITER);

        for (String toEmail : recipients) {
            sendEmailComponent.sendEmail(toEmail, EMAIL_SUBJECT, url);
        }
    }
}
