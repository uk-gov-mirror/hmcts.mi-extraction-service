package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.LineWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(SpringExtension.class)
class PaymentWriteDataComponentImplTest {

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final String PAYMENT_JSON = "{\"id\":\"1\"}";

    @Mock private FilterComponent filterComponent;
    @Mock private LineWriterComponent lineWriterComponent;

    private PaymentWriteDataComponentImpl underTest;

    private final BufferedWriter mockWriter = mock(BufferedWriter.class);

    @BeforeEach
    void setUp() {
        underTest = new PaymentWriteDataComponentImpl(filterComponent, lineWriterComponent);
    }

    @Test
    void givenListOfCoreCaseData_whenWriteData_thenVerifyDataWasWritten() {
        when(filterComponent
                 .filterDataInDateRange(argThat(allOf(hasItem(PAYMENT_JSON))),
                                        eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME), eq(SourceEnum.PAYMENT_HISTORY)))
            .thenReturn(Collections.singletonList(PAYMENT_JSON));

        List<String> dataInput = List.of(PAYMENT_JSON);

        assertEquals(1,
                     underTest.writeData(mockWriter, dataInput, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME, SourceEnum.PAYMENT_HISTORY),
                     "Expected number of records written to be 1.");

        verify(lineWriterComponent, times(1))
            .writeLines(any(BufferedWriter.class), eq(Collections.singletonList(PAYMENT_JSON)));
    }
}
