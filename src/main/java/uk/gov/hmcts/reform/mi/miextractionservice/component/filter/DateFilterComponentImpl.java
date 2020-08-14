package uk.gov.hmcts.reform.mi.miextractionservice.component.filter;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Optional;

import static uk.gov.hmcts.reform.mi.miextractionservice.util.DateUtils.parseDateString;

@Component
public class DateFilterComponentImpl implements DateFilterComponent {

    @Override
    public boolean filterByDate(JsonNode data, SourceProperties sourceProperties, LocalDate fromDate, LocalDate toDate) {
        String dateString = getDataWithCaseInsensitiveFieldName(data, sourceProperties.getDateField());
        ZoneId zone = Optional.ofNullable(sourceProperties.getTimezone())
            .map(timezone -> ZoneId.of(sourceProperties.getTimezone()))
            .orElse(ZoneOffset.UTC);

        LocalDate date = parseDateString(dateString, zone);

        // Plus and minus one day to account for same day dates.
        return date.isAfter(fromDate.minusDays(1L)) && date.isBefore(toDate.plusDays(1L));
    }

    private String getDataWithCaseInsensitiveFieldName(JsonNode data, String dateField) {
        Iterator<String> fieldNames = data.fieldNames();
        String actualFieldName = null;

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            if (fieldName.equalsIgnoreCase(dateField)) {
                actualFieldName = fieldName;
                break;
            }
        }

        try {
            return data.get(actualFieldName).asText();
        } catch (Exception e) {
            throw new ParserException(String.format("Unable to parse date field %s from given json.", dateField), e);
        }
    }
}
