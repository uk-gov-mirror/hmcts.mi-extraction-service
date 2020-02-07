package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CREATED_DATE_FORMATTED;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EXTRACTION_DATE;

@ExtendWith(SpringExtension.class)
public class CoreCaseDataOutputFormatterComponentImplTest {

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Spy
    private DateTimeUtil dateTimeUtil;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private CoreCaseDataOutputFormatterComponentImpl underTest;

    // PMD not picking up message in assertion for some reason.
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    @Test
    public void givenCoreCaseData_whenFormatData_thenReturnOutputCoreCaseData() throws Exception {
        String dataString = DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING;

        CoreCaseData coreCaseData = new ObjectMapper().readValue(dataString, CoreCaseData.class);

        OutputCoreCaseData expected = OutputCoreCaseData
            .builder()
            .extraction_date(TEST_EXTRACTION_DATE)
            .case_metadata_event_id(TEST_CASE_METADATA_EVENT_ID)
            .ce_case_data_id(TEST_CASE_DATA_ID)
            .ce_created_date(TEST_CREATED_DATE_FORMATTED)
            .ce_case_type_id(TEST_CASE_TYPE_ID)
            .ce_case_type_version(TEST_CASE_TYPE_VERSION)
            .ce_state_id(TEST_CASE_STATE_ID)
            .data(TEST_DATA_JSON_STRING)
            .build();

        when(objectMapper.writeValueAsString(any())).thenCallRealMethod();

        assertEquals(expected, underTest.formatData(coreCaseData), "Properties of output does not match expected values.");
    }

    @Test
    public void givenCoreCaseDataWithInvalidData_whenFormatData_thenThrowParserException() throws Exception {
        CoreCaseData coreCaseData = objectMapper.readValue(DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING, CoreCaseData.class);

        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        ReflectionTestUtils.setField(underTest, "objectMapper", mockObjectMapper);

        assertThrows(ParserException.class, () -> underTest.formatData(coreCaseData), "Expected exception was not thrown.");
    }
}
