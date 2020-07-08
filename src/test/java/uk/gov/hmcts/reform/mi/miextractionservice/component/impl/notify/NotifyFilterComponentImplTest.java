package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // PMD somehow not recognising junit assertion messages.
@ExtendWith(SpringExtension.class)
class NotifyFilterComponentImplTest {

    private static final String FILTER_KEY = "filterService";
    private static final String DEFAULT_FILTER_VALUE = "all";

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String NOTIFY_JSON = "{\"created_at\":\"2000-01-01T10:00:00.000000Z\"}";
    private static final String NOTIFY_JSON_FUTURE = "{\"created_at\":\"2002-01-01T10:00:00.000000Z\"}";
    private static final String NOTIFY_JSON_PAST = "{\"created_at\":\"1998-01-01T10:00:00.000000Z\"}";

    private static final NotificationOutput NOTIFY_OUTPUT = NotificationOutput.builder().createdAt("2000-01-01T10:00:00.000000Z").build();
    private static final NotificationOutput NOTIFY_OUTPUT_FUTURE = NotificationOutput.builder().createdAt("2002-01-01T10:00:00.000000Z").build();
    private static final NotificationOutput NOTIFY_OUTPUT_PAST = NotificationOutput.builder().createdAt("1998-01-01T10:00:00.000000Z").build();

    @Mock
    private DataParserComponent<NotificationOutput> dataParserComponent;

    @InjectMocks
    private NotifyFilterComponentImpl underTest;

    @Test
    void givenRangeOfDates_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, DEFAULT_FILTER_VALUE);

        when(dataParserComponent.parse(NOTIFY_JSON)).thenReturn(NOTIFY_OUTPUT);
        when(dataParserComponent.parse(NOTIFY_JSON_FUTURE)).thenReturn(NOTIFY_OUTPUT_FUTURE);
        when(dataParserComponent.parse(NOTIFY_JSON_PAST)).thenReturn(NOTIFY_OUTPUT_PAST);

        List<String> inputData = ImmutableList.of(
            NOTIFY_JSON, NOTIFY_JSON_FUTURE, NOTIFY_JSON_PAST
        );

        List<String> expected = Collections.singletonList(NOTIFY_JSON);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Date filter did not work as expected.");
    }

    @Test
    void givenRangeOfDatesWithSameDayDates_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, DEFAULT_FILTER_VALUE);

        when(dataParserComponent.parse(NOTIFY_JSON)).thenReturn(NOTIFY_OUTPUT);
        when(dataParserComponent.parse(NOTIFY_JSON_FUTURE)).thenReturn(NOTIFY_OUTPUT_FUTURE);
        when(dataParserComponent.parse(NOTIFY_JSON_PAST)).thenReturn(NOTIFY_OUTPUT_PAST);

        List<String> inputData = ImmutableList.of(
            NOTIFY_JSON, NOTIFY_JSON_FUTURE, NOTIFY_JSON_PAST
        );

        List<String> expected = ImmutableList.of(
            NOTIFY_JSON, NOTIFY_JSON_FUTURE, NOTIFY_JSON_PAST
        );

        OffsetDateTime testFromDate = OffsetDateTime.of(1997, 7, 11, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime testToDate = OffsetDateTime.of(2003, 5, 18, 23, 59, 59, 999, ZoneOffset.UTC);

        assertEquals(expected, underTest.filterDataInDateRange(inputData, testFromDate, testToDate),
            "Date filter with on same day date did not work as expected.");
    }

    @Test
    void givenSpecificService_whenFilter_thenReturnDataOnlyWithCaseType() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, "NEWCASETYPE");

        String notifyJsonWithService = "{\"service\":\"NEWCASETYPE\",\"created_at\":\"2000-01-01T10:00:00.000000Z\"}";
        NotificationOutput notifyOutputWithService = NotificationOutput.builder()
            .service("NEWCASETYPE")
            .createdAt("2000-01-01T10:00:00.000000Z")
            .build();

        when(dataParserComponent.parse(NOTIFY_JSON)).thenReturn(NOTIFY_OUTPUT);
        when(dataParserComponent.parse(notifyJsonWithService)).thenReturn(notifyOutputWithService);

        List<String> inputData = ImmutableList.of(
            NOTIFY_JSON, notifyJsonWithService
        );

        List<String> expected = Collections.singletonList(notifyJsonWithService);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Case type filter did not work as expected.");
    }
}
