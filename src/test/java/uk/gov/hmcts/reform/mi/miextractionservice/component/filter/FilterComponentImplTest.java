package uk.gov.hmcts.reform.mi.miextractionservice.component.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class FilterComponentImplTest {

    private FilterComponent classToTest;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        classToTest = new FilterComponentImpl();

        objectMapper = new ObjectMapper();
    }

    @Test
    void givenNoFiltersOnSourceProperty_whenFilter_thenReturnTrue() {
        assertTrue(classToTest.filter(null, SourceProperties.builder().build()),
                   "Filter should return true when nothing to filter.");
    }

    @Test
    void givenMultipleFiltersThatPass_whenFilter_thenReturnTrue() throws Exception {
        final JsonNode jsonNode = objectMapper.readTree("{\"test\":\"data\",\"hello\":\"world\"}");
        final List<String> filters = ImmutableList.of("test=data","hello=world");
        assertTrue(classToTest.filter(jsonNode, SourceProperties.builder().filters(filters).build()),
                   "Filter should return true when all filters pass.");
    }

    @Test
    void givenMultipleFiltersWithOneThatFails_whenFilter_thenReturnFalse() throws Exception {
        final JsonNode jsonNode = objectMapper.readTree("{\"test\":\"fails\",\"hello\":\"world\"}");
        final List<String> filters = ImmutableList.of("test=data","hello=world");
        assertFalse(classToTest.filter(jsonNode, SourceProperties.builder().filters(filters).build()),
                    "Filter should return false when one filter fails to pass.");
    }

    @Test
    void givenInvalidFilter_whenFilter_thenThrowParserException() {
        final List<String> filters = ImmutableList.of("broken=filter=is");
        assertThrows(ParserException.class, () -> classToTest.filter(null, SourceProperties.builder().filters(filters).build()));
    }
}
