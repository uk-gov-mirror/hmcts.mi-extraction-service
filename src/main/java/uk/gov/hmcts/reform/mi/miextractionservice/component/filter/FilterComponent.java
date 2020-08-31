package uk.gov.hmcts.reform.mi.miextractionservice.component.filter;

import com.fasterxml.jackson.databind.JsonNode;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;

public interface FilterComponent {

    boolean filter(JsonNode data, SourceProperties sourceProperties);
}
