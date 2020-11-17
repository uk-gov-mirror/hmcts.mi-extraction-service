package uk.gov.hmcts.reform.mi.miextractionservice.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.YEAR_MONTH_FORMAT;

@Slf4j
public final class DateUtils {

    public static LocalDate parseDateString(String dateString) {
        return parseDateString(dateString, null);
    }

    // To Improve: with library or better algorithm.
    public static LocalDate parseDateString(String dateString, ZoneId zone) {
        ZoneId timezone = Optional.ofNullable(zone).orElse(ZoneOffset.UTC);

        LocalDate localDate;

        try {
            localDate = parseDateTime(dateString, timezone);
        } catch (DateTimeParseException e) {
            log.debug("Unable to parse {} as ZonedDateTime", dateString, e);

            try {
                localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ex) {
                log.debug("Unable to parse {} as LocalDate", dateString, e);

                localDate = LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(dateString)), timezone);
            }
        }

        return localDate;
    }

    private static LocalDate parseDateTime(String dateString, ZoneId timezone) {
        try {
            return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(timezone)
                .toLocalDate();
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) // Legacy Extraction Date Format
                .toLocalDate();
        }
    }

    public static LocalDate getRetrievalDate(String retrieveDate) {
        return Optional.ofNullable(retrieveDate)
            .filter(StringUtils::isNotEmpty)
            .map(fromDate -> parseDateString(retrieveDate))
            .orElse(LocalDate.now().minusDays(1L)); // Default to extracting yesterday's data.
    }

    public static List<String> getListOfYearsAndMonthsBetweenDates(LocalDate fromDate, LocalDate toDate) {
        List<String> dateList = new ArrayList<>();

        LocalDate currentDate = fromDate.withDayOfMonth(1);
        LocalDate finalDate = toDate.withDayOfMonth(28);

        while (currentDate.isBefore(finalDate)) {
            dateList.add(currentDate.format(DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT)));
            currentDate = currentDate.plusMonths(1L);
        }

        return dateList;
    }

    private DateUtils() {
        // Private Constructor
    }
}
