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

        assertEquals(resultDate.getYear(), 2000, "Resultant year is wrong.");
        assertEquals(resultDate.getMonthValue(), 1, "Resultant month is wrong.");
        assertEquals(resultDate.getDayOfMonth(), 1, "Resultant day is wrong.");
    }

}
