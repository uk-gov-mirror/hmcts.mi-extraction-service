package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.payment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@AllArgsConstructor
@Component
@Qualifier("payment")
public class PaymentFilterComponentImpl implements FilterComponent {

    private final DataParserComponent<JsonNode> dataParserComponent;

    @Override
    public List<String> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate, SourceEnum source) {
        return data.stream()
            .filter(line -> {
                JsonNode jsonNode = dataParserComponent.parse(line);

                return filterByDate(jsonNode, fromDate, toDate, source);
            })
            .collect(Collectors.toList());
    }

    private boolean filterByDate(JsonNode jsonNode, OffsetDateTime fromDate, OffsetDateTime toDate, SourceEnum source) {
        String dateFieldName = getDateFieldName(source);

        LocalDate paymentDate =
            LocalDateTime.parse(jsonNode.get(dateFieldName).asText(), ISO_DATE_TIME)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate();

        // Minus and plus 1 day to account to include same day events
        return paymentDate.isAfter(fromDate.toLocalDate().minusDays(1L))
            && paymentDate.isBefore(toDate.toLocalDate().plusDays(1L));
    }

    private String getDateFieldName(SourceEnum source) {
        switch (source) {
            case PAYMENT_HISTORY:
                return "sh_date_updated";
            case PAYMENT_ALLOCATION:
                return "date_created";
            default:
                return "date_updated";
        }
    }
}
