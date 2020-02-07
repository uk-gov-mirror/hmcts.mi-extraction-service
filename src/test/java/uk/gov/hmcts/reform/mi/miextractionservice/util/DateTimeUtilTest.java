package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class DateTimeUtilTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private DateTimeUtil underTest;

    @BeforeEach
    public void setUp() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(0));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    public void givenClock_whenGetCurrentTime_thenReturnOffsetDateTime() {
        OffsetDateTime fixedDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC);
        assertEquals(fixedDateTime, underTest.getCurrentDateTime(), "Wrong time was returned when retrieving.");
    }

    @Test
    public void givenDateString_whenParseDateString_thenReturnOffsetDateTime() {
        OffsetDateTime expectedDate = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(expectedDate, underTest.parseDateString("2000-01-01"), "Parsed date did not match expected date.");
    }

    @Test
    public void givenMillisecondsSinceEpoch_whenGetTimestampFromLong_thenTimestampString() {
        assertEquals("1970-01-01 00:00:00.000", underTest.getTimestampFormatFromLong(0L), "Expected timestamp does not match.");
    }

    @Test
    public void givenTwoDigitMonth_whenGetFormattedMonthNumber_returnTwoDigitMonth() {
        assertEquals("10", underTest.getFormattedMonthNumber(10), "Unexpected format in result.");
    }

    @Test
    public void givenOneDigitMonth_whenGetFormattedMonthNumber_returnTwoDigitMonth() {
        assertEquals("01", underTest.getFormattedMonthNumber(1), "Unexpected format in result.");
    }

    @Test
    public void givenTestDate_whenGetDateFormat_thenReturnDateTimeFormatterThatCanParseYearMonthDay() {
        DateTimeFormatter dateTimeFormatter = underTest.getDateFormat();
        String testDate = "2000-01-01";

        LocalDate resultDate = LocalDate.parse(testDate, dateTimeFormatter);

        assertEquals(2000, resultDate.getYear(), "Resultant year is wrong.");
        assertEquals(1, resultDate.getMonthValue(), "Resultant month is wrong.");
        assertEquals(1, resultDate.getDayOfMonth(), "Resultant day is wrong.");
    }

    @Test
    public void givenFromDateAndToDate_whenGetListOfYearsAndMonthsBetweenDates_thenReturnListOfDateStrings() {
        OffsetDateTime fromDate = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        List<String> expectedList = Arrays.asList(
            "1999-12", "2000-01", "2000-02", "2000-03", "2000-04", "2000-05", "2000-06",
            "2000-07", "2000-08", "2000-09", "2000-10", "2000-11", "2000-12", "2001-01"
        );

        List<String> resultList = underTest.getListOfYearsAndMonthsBetweenDates(fromDate, toDate);

        assertEquals(expectedList, resultList, "List of returned date strings does not match the expected.");
    }

}
