package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CREATED_DATE_FORMATTED;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EXTRACTION_DATE;

@ExtendWith(SpringExtension.class)
public class CsvWriterComponentImplTest {

    private static final String TEST_FILE_NAME = "test";
    private static final String EXPECTED_HEADER_ROW = "\"extraction_date\",\"case_metadata_event_id\",\"ce_case_data_id\","
        + "\"ce_created_date\",\"ce_case_type_id\",\"ce_case_type_version\",\"ce_state_id\",\"data\"";

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

    private String uniqueFileName;

    private CsvWriterComponentImpl underTest;

    @BeforeEach
    public void setUp() {
        uniqueFileName = TEST_FILE_NAME + UUID.randomUUID().toString();

        underTest = new CsvWriterComponentImpl();
    }

    @AfterEach
    public void tearDown() {
        File testFile = new File(uniqueFileName);

        if (testFile.exists()) {
            new File(uniqueFileName).delete();
        }
    }

    @Test
    public void givenValidWriter_whenWriteHeaders_thenWriterWritesHeaders() throws Exception {
        try (Writer writer = mock(Writer.class)) {

            underTest.writeHeadersToCsvFile(writer);

            verify(writer).write(contains(EXPECTED_HEADER_ROW + "\n"));
        }
    }

    @Test
    public void givenValidWriter_whenWriteBeans_thenCsvFileIsCreated() throws Exception {
        try (Writer writer = mock(Writer.class)) {

            underTest.writeBeansWithWriter(writer, Collections.singletonList(TEST_OUTPUT_DATA));

            verify(writer).write(getExpectedDataString() + "\n");
        }
    }

    @Test
    public void givenExceptionOnFlush_whenWriteBeans_thenThrowParserException() throws Exception {
        try (Writer writer = mock(Writer.class)) {
            doThrow(new IOException("Broken on write.")).when(writer).flush();

            assertThrows(ParserException.class, () -> underTest.writeBeansWithWriter(writer, Collections.singletonList(TEST_OUTPUT_DATA)));
        }
    }

    @Test
    public void givenValidFilePathAndBeans_whenWriteBeanToCsv_thenCsvFileIsCreated() throws Exception {
        underTest.writeBeansAsCsvFile(uniqueFileName, Collections.singletonList(TEST_OUTPUT_DATA));

        try (InputStream fileInputStream = Files.newInputStream(Paths.get(uniqueFileName))) {

            List<String> dataAsString = new ReaderUtil().readBytesAsStrings(fileInputStream.readAllBytes());

            String expectedDataRow = getExpectedDataString();

            // Remove generated fields (prefixed with non-word characters) from mutation test if exists
            String actualHeader = dataAsString.get(0).replaceAll(",\"\\W.*\"", "");

            assertEquals(EXPECTED_HEADER_ROW, actualHeader, "Header row did not match expected.");
            assertEquals(expectedDataRow, dataAsString.get(1), "Data row did not match expected.");
        }
    }

    @Test
    public void givenInvalidFilePath_whenWriteBeanToCsv_thenParserExceptionIsThrown() {
        assertThrows(ParserException.class, () -> underTest.writeBeansAsCsvFile("/", Collections.emptyList()));
    }

    private String wrapStringInQuotes(String inputString) {
        return "\"" + inputString + "\"";
    }

    private String getExpectedDataString() {
        // Everything will be wrapped in quotes to prevent data json string commas from affecting actual data
        return String.join(",",
            wrapStringInQuotes(TEST_EXTRACTION_DATE),
            wrapStringInQuotes(TEST_CASE_METADATA_EVENT_ID),
            wrapStringInQuotes(TEST_CASE_DATA_ID),
            wrapStringInQuotes(TEST_CREATED_DATE_FORMATTED),
            wrapStringInQuotes(TEST_CASE_TYPE_ID),
            wrapStringInQuotes(TEST_CASE_TYPE_VERSION),
            wrapStringInQuotes(TEST_CASE_STATE_ID),
            wrapStringInQuotes("{\"\"hello\"\":\"\"world\"\"}")
        );
    }
}
