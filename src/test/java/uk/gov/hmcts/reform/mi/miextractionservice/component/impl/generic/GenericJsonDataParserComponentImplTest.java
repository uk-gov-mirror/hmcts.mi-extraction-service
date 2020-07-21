package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.generic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class GenericJsonDataParserComponentImplTest {

    private GenericJsonDataParserComponentImpl underTest;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        underTest = new GenericJsonDataParserComponentImpl(objectMapper);
    }

    @Test
    void givenJsonString_whenParseData_thenReturnJsonNodeWithFields() {
        String jsonString = "{\"hello\":\"world\"}";

        JsonNode actual = underTest.parse(jsonString);

        assertEquals("world", actual.get("hello").asText(), "Expected json node to have correct key value pair.");
    }

    @Test
    void givenInvalidString_whenParseData_thenThrowParserException() {
        assertThrows(ParserException.class, () -> underTest.parse("hello:world"));
    }
}
