package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EXTRACTION_DATE;

@ExtendWith(SpringExtension.class)
class CoreCaseDataParserComponentImplTest {

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private CoreCaseDataParserComponentImpl underTest;

    @Test
    void givenCoreCaseDataJsonString_whenParseData_thenReturnCoreCaseDataObject() throws JsonProcessingException {
        ObjectMapper objectMapperStub = new ObjectMapper()
            .registerModule(new JavaTimeModule());

        CoreCaseData expected = CoreCaseData.builder()
            .extractionDate(TEST_EXTRACTION_DATE)
            .ceId(Long.parseLong(TEST_CASE_METADATA_EVENT_ID))
            .ceCaseDataId(Long.parseLong(TEST_CASE_DATA_ID))
            .ceCaseTypeId(TEST_CASE_TYPE_ID)
            .ceCaseTypeVersion(Long.parseLong(TEST_CASE_TYPE_VERSION))
            .ceCreatedDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.of("UTC")))
            .ceStateId(TEST_CASE_STATE_ID)
            .ceData(Collections.singletonMap("hello", "world"))
            .build();

        doAnswer(invocation -> objectMapperStub.readValue(DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING, CoreCaseData.class))
            .when(objectMapper).readValue(DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING, CoreCaseData.class);

        assertEquals(expected, underTest.parse(DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING), "Result does not match expected properties.");
    }

    @Test
    void givenInvalidCoreCaseDataJsonString_whenParseData_thenThrowParserException() {
        assertThrows(ParserException.class, () -> underTest.parse("InvalidJsonString"));
    }
}
