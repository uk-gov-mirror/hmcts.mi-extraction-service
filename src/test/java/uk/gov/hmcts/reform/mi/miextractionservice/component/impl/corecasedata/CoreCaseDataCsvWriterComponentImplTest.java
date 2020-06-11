package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.lib.CSVWriterKeepAlive;
import uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.CSVWriterThrowExceptionStub;
import uk.gov.hmcts.reform.mi.miextractionservice.util.ReaderUtil;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_DATA_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_METADATA_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_STATE_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CASE_TYPE_VERSION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_CREATED_DATE_FORMATTED;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_DATA_JSON_STRING;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_DESCRIPTION;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EVENT_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_EXTRACTION_DATE;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_SUMMARY;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.mi.miextractionservice.test.helpers.TestConstants.TEST_USER_LAST_NAME;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
@ExtendWith(SpringExtension.class)
public class CoreCaseDataCsvWriterComponentImplTest {

    private static final String TEST_FILE_NAME = "unit-test";
    private static final String EXPECTED_HEADER_ROW = "\"extraction_date\",\"case_metadata_event_id\",\"ce_case_data_id\","
        + "\"ce_created_date\",\"ce_case_type_id\",\"ce_case_type_version\",\"ce_state_id\",\"ce_state_name\",\"ce_summary\","
        + "\"ce_description\",\"ce_event_id\",\"ce_event_name\",\"ce_user_id\","
        + "\"ce_user_first_name\",\"ce_user_last_name\",\"data\"";

    private static final OutputCoreCaseData TEST_OUTPUT_DATA = OutputCoreCaseData
        .builder()
        .extraction_date(TEST_EXTRACTION_DATE)
        .case_metadata_event_id(TEST_CASE_METADATA_EVENT_ID)
        .ce_case_data_id(TEST_CASE_DATA_ID)
        .ce_created_date(TEST_CREATED_DATE_FORMATTED)
        .ce_case_type_id(TEST_CASE_TYPE_ID)
        .ce_case_type_version(TEST_CASE_TYPE_VERSION)
        .ce_state_id(TEST_CASE_STATE_ID)
        .ce_state_name(TEST_CASE_STATE_NAME)
        .ce_event_name(TEST_EVENT_NAME)
        .ce_summary(TEST_SUMMARY)
        .ce_event_id(TEST_EVENT_ID)
        .ce_user_id(TEST_USER_ID)
        .ce_user_first_name(TEST_USER_FIRST_NAME)
        .ce_user_last_name(TEST_USER_LAST_NAME)
        .ce_description(TEST_DESCRIPTION)
        .data(TEST_DATA_JSON_STRING)
        .build();

    private String uniqueFileName;

    @Mock
    private WriterWrapper writerWrapper;

    @InjectMocks
    private CoreCaseDataCsvWriterComponentImpl underTest;

    private CSVWriterKeepAlive csvWriter;
    private BufferedWriter bufferedWriter;
    private Writer writer;

    @BeforeEach
    public void setUp() throws IOException {
        uniqueFileName = TEST_FILE_NAME + "-" + UUID.randomUUID().toString();

        writer = mock(Writer.class);
        csvWriter = spy(new CSVWriterKeepAlive(writer));
        bufferedWriter = spy(new BufferedWriter(writer));

        when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriter);
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenReturn(bufferedWriter);
    }

    @AfterEach
    public void tearDown() throws Exception {
        File testFile = new File(uniqueFileName);

        if (testFile.exists()) {
            new File(uniqueFileName).delete();
        }

        writer.close();
    }

    @Test
    public void givenValidWriter_whenWriteHeaders_thenWriterWritesHeaders() throws Exception {
        underTest.writeHeadersToCsvFile(writer);

        verify(writer).write(contains(EXPECTED_HEADER_ROW + "\n"));
        verify(writer, times(1)).flush();
        verify(csvWriter, times(1)).close();
    }

    @Test
    public void givenValidWriter_whenWriteBeans_thenCsvFileIsCreated() throws Exception {
        underTest.writeBeansWithWriter(writer, Collections.singletonList(TEST_OUTPUT_DATA));

        verify(writer).write(getExpectedDataString() + "\n");
        verify(writer, times(1)).flush();
        verify(csvWriter, times(1)).close();
    }

    @Test
    public void givenExceptionOnClose_whenWriteHeaders_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(csvWriter).close();

        assertThrows(ParserException.class, () -> underTest.writeHeadersToCsvFile(writer));

        verify(writer, never()).flush();
    }

    @Test
    public void givenExceptionOnClose_whenWriteBeans_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(csvWriter).close();

        assertThrows(ParserException.class, () -> underTest.writeBeansWithWriter(writer, Collections.singletonList(TEST_OUTPUT_DATA)));

        verify(writer, never()).flush();
    }

    @Test
    public void givenExceptionOnClose_whenWriteBeanToCsv_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(bufferedWriter).close();

        assertThrows(ParserException.class, () -> underTest.writeBeansAsCsvFile(uniqueFileName, Collections.singletonList(TEST_OUTPUT_DATA)));

        // One flush for write header and one flush for write line. Final flush for writer close not called due to exception.
        verify(writer, times(2)).flush();
    }

    @Test
    public void givenExceptionOnWrite_whenWriteHeaders_thenExceptionCaughtAndWriterIsClosed() throws Exception {
        try (CSVWriterThrowExceptionStub csvWriterThrowsException = spy(new CSVWriterThrowExceptionStub(writer))) {
            when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriterThrowsException);

            assertThrows(RuntimeException.class, () -> underTest.writeHeadersToCsvFile(writer));

            verify(csvWriterThrowsException, times(1)).close();
        }
    }

    @Test
    public void givenExceptionOnWrite_whenWriteBeans_thenExceptionCaughtAndWriterIsClosed() throws Exception {
        try (CSVWriterThrowExceptionStub csvWriterThrowsException = spy(new CSVWriterThrowExceptionStub(writer))) {
            when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriterThrowsException);

            assertThrows(RuntimeException.class, () -> underTest.writeBeansWithWriter(writer, Collections.singletonList(TEST_OUTPUT_DATA)));

            verify(csvWriterThrowsException, times(1)).close();
        }
    }

    @Test
    public void givenExceptionOnWrite_whenWriteBeanToCsv_thenExceptionCaughtAndWriterIsClosed() throws Exception {
        try (CSVWriterThrowExceptionStub csvWriterThrowsException = spy(new CSVWriterThrowExceptionStub(writer))) {
            when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriterThrowsException);

            assertThrows(RuntimeException.class, () -> underTest.writeBeansAsCsvFile(uniqueFileName, Collections.singletonList(TEST_OUTPUT_DATA)));

            verify(csvWriterThrowsException, times(1)).close();
        }
    }

    @Test
    public void givenValidFilePathAndBeans_whenWriteBeanToCsv_thenCsvFileIsCreated() throws Exception {
        when(writerWrapper.getCsvWriter(any())).thenCallRealMethod();
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenCallRealMethod();

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
    public void givenInvalidFilePath_whenWriteBeanToCsv_thenParserExceptionIsThrown() throws Exception {
        when(writerWrapper.getCsvWriter(any())).thenCallRealMethod();
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenCallRealMethod();

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
            wrapStringInQuotes(TEST_CASE_STATE_NAME),
            wrapStringInQuotes(TEST_SUMMARY),
            wrapStringInQuotes(TEST_DESCRIPTION),
            wrapStringInQuotes(TEST_EVENT_ID),
            wrapStringInQuotes(TEST_EVENT_NAME),
            wrapStringInQuotes(TEST_USER_ID),
            wrapStringInQuotes(TEST_USER_FIRST_NAME),
            wrapStringInQuotes(TEST_USER_LAST_NAME),
            wrapStringInQuotes("{\"\"hello\"\":\"\"world\"\"}")
        );
    }
}
