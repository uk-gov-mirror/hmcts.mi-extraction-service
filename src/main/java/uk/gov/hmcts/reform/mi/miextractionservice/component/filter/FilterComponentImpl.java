package uk.gov.hmcts.reform.mi.miextractionservice.component.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.util.regex.Pattern;

@Slf4j
@Component
public class FilterComponentImpl implements FilterComponent {

    private static final int MAX_FILTER_PARTS = 2;

    @Override
    public boolean filter(JsonNode data, SourceProperties sourceProperties) {
        boolean passFilters = true;

        if (sourceProperties.getFilters() != null) {
            for (String filter : sourceProperties.getFilters()) {
                String[] parsedFilter = buildFilter(filter);
                passFilters = Boolean
                    .logicalAnd(
                        passFilters,
                        Pattern.compile(parsedFilter[1]).matcher(data.get(parsedFilter[0]).asText()).matches()
                    );
            }
        }

        return passFilters;
    }

    private String[] buildFilter(String filterString) {
        String[] parsedFilter = filterString.split("=");
        if (parsedFilter.length != MAX_FILTER_PARTS) {
            throw new ParserException("Filter string is not in the correct format: " + filterString);
        }

        return parsedFilter;
    }
}
