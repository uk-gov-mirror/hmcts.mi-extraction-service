package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DateTimeUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String YEAR_MONTH_FORMAT = "yyyy-MM";

    @Autowired
    private Clock clock;

    public OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now(clock);
    }

    public OffsetDateTime parseDateString(String dateString) {
        return OffsetDateTime.of(
            LocalDate.parse(dateString), LocalTime.MIDNIGHT, ZoneOffset.UTC
        );
    }

    public String getTimestampFormatFromLong(long milliseconds) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneOffset.UTC);
        return dateTimeFormatter.format(Instant.ofEpochMilli(milliseconds));
    }

    public String getFormattedMonthNumber(int rawMonthValue) {
        return String.format("%02d", rawMonthValue);
    }

    public DateTimeFormatter getDateFormat() {
        return DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    public List<String> getListOfYearsAndMonthsBetweenDates(OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<String> dateList = new ArrayList<>();

        LocalDate currentDate = fromDate.toLocalDate().withDayOfMonth(1);
        LocalDate finalDate = toDate.toLocalDate().withDayOfMonth(28);

        do {
            dateList.add(currentDate.format(DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT)));
            currentDate = currentDate.plusMonths(1L);
        } while (currentDate.isBefore(finalDate));

        return dateList;
    }
}
