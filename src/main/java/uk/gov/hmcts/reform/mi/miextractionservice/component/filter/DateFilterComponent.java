package uk.gov.hmcts.reform.mi.miextractionservice.component.filter;

import com.fasterxml.jackson.databind.JsonNode;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;

import java.time.LocalDate;

public interface DateFilterComponent {

    boolean filterByDate(JsonNode data, SourceProperties sourceProperties, LocalDate fromDate, LocalDate toDate);
}
