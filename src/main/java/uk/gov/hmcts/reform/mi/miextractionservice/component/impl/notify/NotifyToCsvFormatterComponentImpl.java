package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.micore.model.NotifyTemplate;
import uk.gov.hmcts.reform.mi.miextractionservice.component.NotifyToCsvFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.CsvNotificationOutput;

import java.util.Optional;

@Component
public class NotifyToCsvFormatterComponentImpl implements NotifyToCsvFormatterComponent {

    @Override
    public CsvNotificationOutput formatData(NotificationOutput notificationOutput) {
        NotifyTemplate notifyTemplate = Optional.of(notificationOutput.getTemplate())
            .orElse(NotifyTemplate.builder().build());

        return CsvNotificationOutput
            .builder()
            .extractionDate(notificationOutput.getExtractionDate())
            .id(notificationOutput.getId())
            .service(notificationOutput.getService())
            .reference(notificationOutput.getReference())
            .emailAddress(notificationOutput.getEmailAddress())
            .phoneNumber(notificationOutput.getPhoneNumber())
            .lineOne(notificationOutput.getLineOne())
            .lineTwo(notificationOutput.getLineTwo())
            .lineThree(notificationOutput.getLineThree())
            .lineFour(notificationOutput.getLineFour())
            .lineFive(notificationOutput.getLineFive())
            .lineSix(notificationOutput.getLineSix())
            .postcode(notificationOutput.getPostcode())
            .type(notificationOutput.getType())
            .status(notificationOutput.getStatus())
            .templateId(notifyTemplate.getId())
            .templateVersion(notifyTemplate.getVersion())
            .templateUri(notifyTemplate.getUri())
            .templateName(notifyTemplate.getName())
            .body(notificationOutput.getBody())
            .subject(notificationOutput.getSubject())
            .createdAt(notificationOutput.getCreatedAt())
            .sentAt(notificationOutput.getSentAt())
            .completedAt(notificationOutput.getCompletedAt())
            .build();
    }
}
