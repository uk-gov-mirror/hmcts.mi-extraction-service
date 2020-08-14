package uk.gov.hmcts.reform.mi.miextractionservice.component.parser;

import com.fasterxml.jackson.databind.JsonNode;

public interface DataParserComponent {

    JsonNode parseJsonString(String data);
}
