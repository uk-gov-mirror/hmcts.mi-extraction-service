package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.impl.generic.GenericJsonDataParserComponentImpl;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class PaymentFilterComponentImplTest {

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String PAYMENT_HISTORY_JSON = "{\"sh_date_updated\":\"2000-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_HISTORY_FUTURE = "{\"sh_date_updated\":\"2002-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_HISTORY_PAST = "{\"sh_date_updated\":\"1998-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_ALLOCATION_JSON = "{\"date_created\":\"2000-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_ALLOCATION_FUTURE = "{\"date_created\":\"2002-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_ALLOCATION_PAST = "{\"date_created\":\"1998-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_UPDATED_JSON = "{\"date_updated\":\"2000-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_UPDATED_FUTURE = "{\"date_updated\":\"2002-01-01T10:00:00.000000Z\"}";
    private static final String PAYMENT_UPDATED_PAST = "{\"date_updated\":\"1998-01-01T10:00:00.000000Z\"}";

    private PaymentFilterComponentImpl underTest;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        DataParserComponent<JsonNode> dataParserComponent = new GenericJsonDataParserComponentImpl(objectMapper);
        underTest = new PaymentFilterComponentImpl(dataParserComponent);
    }

    @Test
    void givenRangeOfDatesForPaymentHistory_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        List<String> inputData = ImmutableList.of(
            PAYMENT_HISTORY_JSON, PAYMENT_HISTORY_FUTURE, PAYMENT_HISTORY_PAST
        );

        List<String> expected = Collections.singletonList(PAYMENT_HISTORY_JSON);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME, SourceEnum.PAYMENT_HISTORY),
                     "Date filter for payment history did not work as expected.");
    }

    @Test
    void givenRangeOfDatesWithSameDayDates_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        List<String> inputData = ImmutableList.of(
            PAYMENT_HISTORY_JSON, PAYMENT_HISTORY_FUTURE, PAYMENT_HISTORY_PAST
        );

        List<String> expected = ImmutableList.of(
            PAYMENT_HISTORY_JSON, PAYMENT_HISTORY_FUTURE, PAYMENT_HISTORY_PAST
        );

        OffsetDateTime testFromDate = OffsetDateTime.of(1997, 7, 11, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime testToDate = OffsetDateTime.of(2003, 5, 18, 23, 59, 59, 999, ZoneOffset.UTC);

        assertEquals(expected, underTest.filterDataInDateRange(inputData, testFromDate, testToDate, SourceEnum.PAYMENT_HISTORY),
                     "Date filter with on same day date did not work as expected.");
    }

    @Test
    void givenRangeOfDatesForPaymentAllocation_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        List<String> inputData = ImmutableList.of(
            PAYMENT_ALLOCATION_JSON, PAYMENT_ALLOCATION_FUTURE, PAYMENT_ALLOCATION_PAST
        );

        List<String> expected = Collections.singletonList(PAYMENT_ALLOCATION_JSON);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME, SourceEnum.PAYMENT_ALLOCATION),
                     "Date filter for payment allocation did not work as expected.");
    }

    @Test
    void givenRangeOfDatesForPaymentRemission_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        List<String> inputData = ImmutableList.of(
            PAYMENT_UPDATED_JSON, PAYMENT_UPDATED_FUTURE, PAYMENT_UPDATED_PAST
        );

        List<String> expected = Collections.singletonList(PAYMENT_UPDATED_JSON);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME, SourceEnum.PAYMENT_REMISSION),
                     "Date filter for payment remission did not work as expected.");
    }

    @Test
    void givenRangeOfDatesForPaymentFee_whenFilterByDate_thenReturnDataOnlyWithinDates() {
        List<String> inputData = ImmutableList.of(
            PAYMENT_UPDATED_JSON, PAYMENT_UPDATED_FUTURE, PAYMENT_UPDATED_PAST
        );

        List<String> expected = Collections.singletonList(PAYMENT_UPDATED_JSON);
        assertEquals(expected, underTest.filterDataInDateRange(inputData, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME, SourceEnum.PAYMENT_FEE),
                     "Date filter for payment fee did not work as expected.");
    }
}
