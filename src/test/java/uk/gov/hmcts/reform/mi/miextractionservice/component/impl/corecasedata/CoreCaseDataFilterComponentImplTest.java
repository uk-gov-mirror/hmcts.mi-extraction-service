package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_AS_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_NEW_CASETYPE;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_NEW_CASETYPE_AS_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_FUTURE;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_PAST;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA;

@ExtendWith(SpringExtension.class)
class CoreCaseDataFilterComponentImplTest {

    private static final String FILTER_KEY = "filterCaseType";
    private static final String DEFAULT_FILTER_VALUE = "all";

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private DataParserComponent<CoreCaseData> dataParserComponent;

    @InjectMocks
    private CoreCaseDataFilterComponentImpl underTest;

    @Test
    void givenRangeOfDates_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, DEFAULT_FILTER_VALUE);

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_FUTURE)).thenReturn(TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_PAST)).thenReturn(TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA);

        List<String> inputData = ImmutableList.of(
            TEST_CCD_JSONL, TEST_CCD_JSONL_OUTDATED_FUTURE, TEST_CCD_JSONL_OUTDATED_PAST
        );

        List<String> expected = Collections.singletonList(TEST_CCD_JSONL);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Date filter did not work as expected.");
    }

    @Test
    void givenRangeOfDatesWithSameDayDates_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, DEFAULT_FILTER_VALUE);

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_FUTURE)).thenReturn(TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_OUTDATED_PAST)).thenReturn(TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA);

        List<String> inputData = ImmutableList.of(
            TEST_CCD_JSONL, TEST_CCD_JSONL_OUTDATED_FUTURE, TEST_CCD_JSONL_OUTDATED_PAST
        );

        List<String> expected = ImmutableList.of(
            TEST_CCD_JSONL, TEST_CCD_JSONL_OUTDATED_FUTURE, TEST_CCD_JSONL_OUTDATED_PAST
        );

        OffsetDateTime testFromDate = OffsetDateTime.of(1998, 7, 11, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime testToDate = OffsetDateTime.of(2001, 5, 18, 23, 59, 59, 999, ZoneOffset.UTC);

        assertEquals(expected, underTest.filterDataInDateRange(inputData, testFromDate, testToDate),
            "Date filter with on same day date did not work as expected.");
    }

    @Test
    void givenSpecificCaseType_whenFilter_thenReturnDataOnlyWithCaseType() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, "NEWCASETYPE");

        when(dataParserComponent.parse(TEST_CCD_JSONL)).thenReturn(TEST_CCD_JSONL_AS_CORE_CASE_DATA);
        when(dataParserComponent.parse(TEST_CCD_JSONL_NEW_CASETYPE)).thenReturn(TEST_CCD_JSONL_NEW_CASETYPE_AS_CORE_CASE_DATA);

        List<String> inputData = ImmutableList.of(
            TEST_CCD_JSONL, TEST_CCD_JSONL_NEW_CASETYPE
        );

        List<String> expected = Collections.singletonList(TEST_CCD_JSONL_NEW_CASETYPE);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME),
            "Case type filter did not work as expected.");
    }
}
