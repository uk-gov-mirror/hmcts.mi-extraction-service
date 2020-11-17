package uk.gov.hmcts.reform.mi.miextractionservice.client.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

@Component
public class GovUkNotifyClient extends NotificationClient {

    @Autowired
    public GovUkNotifyClient(@Value("${mail.gov-uk.api-key}") String apiKey) {
        super(apiKey);
    }
}
