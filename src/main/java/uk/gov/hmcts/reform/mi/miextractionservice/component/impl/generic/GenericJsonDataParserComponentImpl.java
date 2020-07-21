package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.generic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

@AllArgsConstructor
@Component
public class GenericJsonDataParserComponentImpl implements DataParserComponent<JsonNode> {

    private final ObjectMapper objectMapper;

    @Override
    public JsonNode parse(String data) {
        try {
            return objectMapper.readValue(data, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new ParserException("Unable to map given data to JsonNode.", e);
        }
    }
}
