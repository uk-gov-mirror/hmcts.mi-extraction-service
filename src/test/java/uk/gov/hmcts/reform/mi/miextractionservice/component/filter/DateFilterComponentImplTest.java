package uk.gov.hmcts.reform.mi.miextractionservice.component.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class DateFilterComponentImplTest {

    private DateFilterComponent classToTest;

    private ObjectMapper objectMapper;
    private SourceProperties sourceProperties;

    @BeforeEach
    void setUp() {
        classToTest = new DateFilterComponentImpl();

        objectMapper = new ObjectMapper();
        sourceProperties = SourceProperties.builder().dateField("date_field").build();
    }

    @Test
    void givenDateDataIsBetweenGivenDates_whenFilterByDate_thenReturnTrue() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"date_field\":\"2000-01-01\"}");
        final LocalDate fromDate = LocalDate.parse("1999-01-01");
        final LocalDate toDate = LocalDate.parse("2001-01-01");

        assertTrue(classToTest.filterByDate(data, sourceProperties, fromDate, toDate),
                   "True should be returned for date between range.");
    }

    @Test
    void givenDateDataIsBetweenGivenDatesWithGivenTimezone_whenFilterByDate_thenReturnTrue() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"date_field\":\"2001-02-02T02:00:00.000Z\"}");
        final LocalDate fromDate = LocalDate.parse("1999-02-01");
        final LocalDate toDate = LocalDate.parse("2001-02-01");

        sourceProperties = SourceProperties.builder().dateField("date_field").timezone("America/New_York").build();

        assertTrue(classToTest.filterByDate(data, sourceProperties, fromDate, toDate),
                   "True should be returned for date between range with a timezone.");
    }

    @Test
    void givenDateDataIsAfterToDate_whenFilterByDate_thenReturnFalse() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"date_field\":\"2001-03-02T02:00:00.000Z\"}");
        final LocalDate fromDate = LocalDate.parse("1999-03-01");
        final LocalDate toDate = LocalDate.parse("2001-03-01");

        assertFalse(classToTest.filterByDate(data, sourceProperties, fromDate, toDate),
                    "False should be returned when date is in future to date range.");
    }

    @Test
    void givenDateDataIsBeforeFromDate_whenFilterByDate_thenReturnFalse() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"date_field\":\"1999-03-30\"}");
        final LocalDate fromDate = LocalDate.parse("1999-04-01");
        final LocalDate toDate = LocalDate.parse("2001-04-01");

        assertFalse(classToTest.filterByDate(data, sourceProperties, fromDate, toDate),
                    "False should be returned when date is in past to date range.");
    }

    @Test
    void givenDateDataIsSameDate_whenFilterByDate_thenReturnTrue() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"date_field\":\"1999-05-01\"}");
        final LocalDate fromDate = LocalDate.parse("1999-05-01");
        final LocalDate toDate = LocalDate.parse("1999-05-01");

        assertTrue(classToTest.filterByDate(data, sourceProperties, fromDate, toDate),
                   "True should be returned when date is same as date range.");
    }

    @Test
    void givenCaseSensitiveDateDataIsBetweenGivenDates_whenFilterByDate_thenReturnTrue() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"DaTe_FiElD\":\"2000-06-01\"}");
        final LocalDate fromDate = LocalDate.parse("1999-06-01");
        final LocalDate toDate = LocalDate.parse("2001-06-01");

        assertTrue(classToTest.filterByDate(data, sourceProperties, fromDate, toDate),
                   "True should be returned for date between range.");
    }

    @Test
    void givenDateFieldNotFound_whenFilterByDate_thenThrowParserException() throws Exception {
        final JsonNode data = objectMapper.readTree("{\"Not_A_Date_Field\":\"2000-07-01\"}");
        final LocalDate fromDate = LocalDate.parse("1999-07-01");
        final LocalDate toDate = LocalDate.parse("2001-07-01");

        assertThrows(ParserException.class, () -> classToTest.filterByDate(data, sourceProperties, fromDate, toDate));
    }
}
