package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CREATED_DATE;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EXTRACTION_DATE;

@ExtendWith(SpringExtension.class)
public class CoreCaseDataParserComponentImplTest {

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private CoreCaseDataParserComponentImpl underTest;

    @Test
    public void givenCoreCaseDataJsonString_whenParseData_thenReturnCoreCaseDataObject() {
        CoreCaseData expected = CoreCaseData.builder()
            .extractionDate(TEST_EXTRACTION_DATE)
            .caseMetadataEventId(Long.parseLong(TEST_CASE_METADATA_EVENT_ID))
            .ceCaseDataId(Long.parseLong(TEST_CASE_DATA_ID))
            .ceCaseTypeId(TEST_CASE_TYPE_ID)
            .ceCaseTypeVersion(Long.parseLong(TEST_CASE_TYPE_VERSION))
            .ceCreatedDate(Long.parseLong(TEST_CREATED_DATE))
            .ceStateId(TEST_CASE_STATE_ID)
            .ceData(Collections.singletonMap("hello", "world"))
            .build();

        assertEquals(expected, underTest.parse(DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING), "Result does not match expected properties.");
    }

    @Test
    public void givenInvalidCoreCaseDataJsonString_whenParseData_thenThrowParserException() {
        assertThrows(ParserException.class, () -> underTest.parse("InvalidJsonString"));
    }
}
