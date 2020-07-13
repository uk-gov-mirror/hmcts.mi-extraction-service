package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_TIMESTAMP_FORMAT;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NO_FILTER_VALUE;

@Component
@Qualifier("notify")
public class NotifyFilterComponentImpl implements FilterComponent {

    @Value("${filter.notify.service}")
    private String filterService;

    @Autowired
    private DataParserComponent<NotificationOutput> dataParserComponent;

    @Override
    public List<String> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {
        return data.stream()
            .filter(line -> {
                NotificationOutput notification = dataParserComponent.parse(line);

                return filterByDate(notification, fromDate, toDate) && filterByService(notification);
            })
            .collect(Collectors.toList());
    }

    private boolean filterByDate(NotificationOutput notificationOutput, OffsetDateTime fromDate, OffsetDateTime toDate) {
        LocalDate notificationCreatedDate =
            LocalDateTime.parse(notificationOutput.getCreatedAt(), DateTimeFormatter.ofPattern(NOTIFY_TIMESTAMP_FORMAT))
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Europe/London"))// Notify data is in BST
                .toLocalDate();

        // Minus and plus 1 day to account to include same day events
        return notificationCreatedDate.isAfter(fromDate.toLocalDate().minusDays(1L))
            && notificationCreatedDate.isBefore(toDate.toLocalDate().plusDays(1L));
    }

    private boolean filterByService(NotificationOutput notificationOutput) {
        if (filterService.equalsIgnoreCase(NO_FILTER_VALUE)) {
            return true;
        }

        return filterService.equalsIgnoreCase(notificationOutput.getService());
    }
}
