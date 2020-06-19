package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NEWLINE_DELIMITER;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CREATED_DATE_FORMATTED;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EXTRACTION_DATE;

@ExtendWith(SpringExtension.class)
class CoreCaseDataJsonlWriterComponentImplTest {

    private static final OutputCoreCaseData TEST_OUTPUT_DATA = OutputCoreCaseData
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
    private static final List<OutputCoreCaseData> TEST_OUTPUT_LIST = Collections.singletonList(TEST_OUTPUT_DATA);

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private CoreCaseDataJsonlWriterComponentImpl underTest;

    private Writer writer;

    @BeforeEach
    void setUp() {
        writer = mock(Writer.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        writer.close();
    }

    @Test
    void givenValidWriter_whenWriteBeans_thenLinesAreWritten() throws Exception {
        underTest.writeLinesAsJsonl(writer, TEST_OUTPUT_LIST);

        verify(writer, times(1)).write(getExpectedDataString());
        verify(writer, times(1)).write(NEWLINE_DELIMITER);
    }

    @Test
    void givenExceptionOnClose_whenWriteBeans_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(writer).write(anyString());

        assertThrows(ParserException.class, () -> underTest.writeLinesAsJsonl(writer, TEST_OUTPUT_LIST));

        verify(writer, never()).flush();
    }

    private String getExpectedDataString() {
        try {
            return objectMapper.writeValueAsString(TEST_OUTPUT_DATA);
        } catch (JsonProcessingException e) {
            fail("Unable to parse test data.");
        }

        return "";
    }
}
