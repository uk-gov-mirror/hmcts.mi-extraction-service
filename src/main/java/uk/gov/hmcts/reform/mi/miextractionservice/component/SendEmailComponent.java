package uk.gov.hmcts.reform.mi.miextractionservice.component;

public interface SendEmailComponent {

    void sendEmail(String emailAddress, String subject, String content);
}
