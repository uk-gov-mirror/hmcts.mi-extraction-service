package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class DateUtilsTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2000, 1, 1);

    @Test
    void givenValidZonedDateTimeString_whenParseDate_thenReturnCorrectLocalDate() {
        assertEquals(TEST_DATE, DateUtils.parseDateString("2000-01-01T10:00:00.000Z"),
                     "Correct date should be returned for valid ISO_DATE_TIME format.");
    }

    @Test
    void givenValidExtractionDateTimeString_whenParseDate_thenReturnCorrectLocalDate() {
        assertEquals(TEST_DATE, DateUtils.parseDateString("20000101-1000"),
                     "Correct date should be returned for valid extraction date format.");
    }

    @Test
    void givenValidDateString_whenParseDate_thenReturnCorrectLocalDate() {
        assertEquals(TEST_DATE, DateUtils.parseDateString("2000-01-01"),
                     "Correct date should be returned for valid ISO_DATE format.");
    }

    @Test
    void givenValidDateInMilliseconds_whenParseDate_thenReturnCorrectLocalDate() {
        assertEquals(TEST_DATE, DateUtils.parseDateString("946684808080"),
                     "Correct date should be returned for date in milliseconds epoch format.");
    }

    @Test
    void givenValidZonedDateTimeStringWithTimezone_whenParseDate_thenReturnCorrectLocalDate() {
        assertEquals(TEST_DATE, DateUtils.parseDateString("2000-01-02T01:00:00.000Z", ZoneId.of("America/New_York")),
                     "Correct date should be returned given valid date in non UTC timezone.");
    }

    @Test
    void givenDateString_whenRetrieveAsLocalDate_thenReturnLocalDate() {
        assertEquals(TEST_DATE, DateUtils.getRetrievalDate("2000-01-01"),
                     "Correct date should be returned given valid date string.");
    }

    @Test
    void givenBlankString_whenRetrieveAsLocalDate_thenReturnLocalDateEqualToYesterday() {
        assertEquals(LocalDate.now().minusDays(1L), DateUtils.getRetrievalDate(""),
                     "Yesterday's date should be returned when empty string is given as date.");
    }

    @Test
    void givenNullValue_whenRetrieveAsLocalDate_thenReturnLocalDateEqualToYesterday() {
        assertEquals(LocalDate.now().minusDays(1L), DateUtils.getRetrievalDate(null),
                     "Yesterday's date should be returned when null value is given as date.");
    }

    @Test
    void givenTwoDates_whenGetListOfYearsAndMonthsBetweenDates_returnListOfBetweenDatesInYearMonthFormat() {
        List<String> expectedDatesList = new ArrayList<>();
        expectedDatesList.add("1990-09");
        expectedDatesList.add("1990-10");
        expectedDatesList.add("1990-11");
        expectedDatesList.add("1990-12");
        expectedDatesList.add("1991-01");
        expectedDatesList.add("1991-02");
        expectedDatesList.add("1991-03");

        LocalDate fromDate = LocalDate.of(1990, 9, 1);
        LocalDate toDate = LocalDate.of(1991, 3, 1);

        assertEquals(expectedDatesList, DateUtils.getListOfYearsAndMonthsBetweenDates(fromDate, toDate),
                     "List should contain all year-month values between given dates.");
    }

    @Test
    void givenTwoDatesInWrongOrder_whenGetListOfYearsAndMonthsBetweenDates_returnEmptyList() {
        LocalDate fromDate = LocalDate.of(3000, 9, 1);
        LocalDate toDate = LocalDate.of(2000, 3, 1);

        assertEquals(Collections.emptyList(), DateUtils.getListOfYearsAndMonthsBetweenDates(fromDate, toDate),
                     "List should be empty if to date is in past compared to from date.");
    }
}
