package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.FilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.JsonlWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_AS_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CCD_JSONL_OUTDATED_FUTURE;

@ExtendWith(SpringExtension.class)
public class CoreCaseDataWriteDataComponentImplTest {

    private static final OffsetDateTime TEST_FROM_DATE_TIME = OffsetDateTime.of(1999, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime TEST_TO_DATE_TIME = OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private FilterComponent<CoreCaseData> filterComponent;

    @Mock
    private CoreCaseDataFormatterComponent<OutputCoreCaseData> coreCaseDataFormatterComponent;

    @Mock
    private JsonlWriterComponent<OutputCoreCaseData> jsonlWriterComponent;

    @InjectMocks
    private CoreCaseDataWriteDataComponentImpl underTest;

    @Test
    void givenListOfCoreCaseData_whenWriteData_thenVerifyDataWasWritten() {
        when(filterComponent
            .filterDataInDateRange(argThat(allOf(hasItem(TEST_CCD_JSONL), hasItem(TEST_CCD_JSONL_OUTDATED_FUTURE))),
                eq(TEST_FROM_DATE_TIME), eq(TEST_TO_DATE_TIME)))
            .thenReturn(Collections.singletonList(TEST_CCD_JSONL_AS_CORE_CASE_DATA));

        when(coreCaseDataFormatterComponent.formatData(TEST_CCD_JSONL_AS_CORE_CASE_DATA)).thenReturn(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA);

        List<String> dataInput = List.of(TEST_CCD_JSONL, TEST_CCD_JSONL_OUTDATED_FUTURE);

        underTest.writeData(mock(BufferedWriter.class), dataInput, TEST_FROM_DATE_TIME, TEST_TO_DATE_TIME);

        verify(jsonlWriterComponent, times(1))
            .writeLinesAsJsonl(any(BufferedWriter.class), eq(Collections.singletonList(TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA)));
    }
}
