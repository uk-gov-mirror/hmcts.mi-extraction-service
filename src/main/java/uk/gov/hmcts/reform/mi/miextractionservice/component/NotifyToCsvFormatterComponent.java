package uk.gov.hmcts.reform.mi.miextractionservice.component;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.CsvNotificationOutput;

public interface NotifyToCsvFormatterComponent {

    CsvNotificationOutput formatData(NotificationOutput data);
}
