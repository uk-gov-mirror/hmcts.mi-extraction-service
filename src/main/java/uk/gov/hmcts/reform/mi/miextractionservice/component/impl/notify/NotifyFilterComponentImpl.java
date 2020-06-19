package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NOTIFY_TIMESTAMP_FORMAT;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NO_FILTER_VALUE;

@Component
public class NotifyFilterComponentImpl implements FilterComponent<NotificationOutput> {

    @Value("${filter.notify.service}")
    private String filterService;

    @Autowired
    private DataParserComponent<NotificationOutput> dataParserComponent;

    @Override
    public List<NotificationOutput> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate) {

        List<NotificationOutput> filteredData = data.stream()
            .map(dataRow -> dataParserComponent.parse(dataRow))
            .filter(notification -> {
                if (Objects.isNull(notification)) {
                    return false;
                }

                LocalDate notificationCreatedDate =
                    LocalDateTime.parse(notification.getCreatedAt(), DateTimeFormatter.ofPattern(NOTIFY_TIMESTAMP_FORMAT))
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Europe/London"))// Notify data is in BST
                        .toLocalDate();

                // Minus and plus 1 day to account to include same day events
                return notificationCreatedDate.isAfter(fromDate.toLocalDate().minusDays(1L))
                    && notificationCreatedDate.isBefore(toDate.toLocalDate().plusDays(1L));
            })
            .collect(Collectors.toList());

        filteredData = filterByService(filteredData);

        return filteredData;
    }

    private List<NotificationOutput> filterByService(List<NotificationOutput> data) {
        return data.stream()
            .filter(notification -> {
                if (filterService.equalsIgnoreCase(NO_FILTER_VALUE)) {
                    return true;
                } else {
                    return filterService.equalsIgnoreCase(notification.getService());
                }
            }).collect(Collectors.toList());
    }
}
