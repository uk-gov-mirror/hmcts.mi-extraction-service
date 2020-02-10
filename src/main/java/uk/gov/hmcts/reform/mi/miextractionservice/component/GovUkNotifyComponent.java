package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.util.Map;

public interface GovUkNotifyComponent {

    void sendNotifyEmail(String templateId, String email, Map<String, Object> parameters);
}
