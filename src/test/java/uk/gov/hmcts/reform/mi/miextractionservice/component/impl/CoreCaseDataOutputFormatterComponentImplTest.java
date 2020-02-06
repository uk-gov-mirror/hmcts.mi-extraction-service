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
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_CREATED_DATE_FORMATTED;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.TestConstants.TEST_EXTRACTION_DATE;

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

        OutputCoreCaseData expected = new OutputCoreCaseData(
            TEST_EXTRACTION_DATE,
            TEST_CASE_METADATA_EVENT_ID,
            TEST_CASE_DATA_ID,
            TEST_CREATED_DATE_FORMATTED,
            TEST_CASE_TYPE_ID,
            TEST_CASE_TYPE_VERSION,
            TEST_CASE_STATE_ID,
            TEST_DATA_JSON_STRING
        );

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
