package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.CsvNotificationOutput;
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

@SuppressWarnings({"PMD.TooManyMethods"})
@ExtendWith(SpringExtension.class)
class NotifyCsvWriterComponentImplTest {

    private static final String TEST_FILE_NAME = "unit-test";
    private static final CsvNotificationOutput TEST_OUTPUT_DATA = CsvNotificationOutput.builder()
        .id("notificationId")
        .createdAt("createdTimestamp")
        .build();

    private static final String EXPECTED_HEADER_OUTPUT = "\"extraction_date\",\"id\",\"service\",\"reference\","
        + "\"email_address\",\"phone_number\",\"line_1\",\"line_2\",\"line_3\",\"line_4\",\"line_5\",\"line_6\","
        + "\"postcode\",\"type\",\"status\",\"template_id\",\"template_version\",\"template_uri\",\"template_name\","
        + "\"body\",\"subject\",\"created_at\",\"sent_at\",\"completed_at\"";

    private String uniqueFileName;

    @Mock
    private WriterWrapper writerWrapper;

    @InjectMocks
    private NotifyCsvWriterComponentImpl underTest;

    private CSVWriterKeepAlive csvWriter;
    private BufferedWriter bufferedWriter;
    private Writer writer;

    @BeforeEach
    void setUp() throws IOException {
        uniqueFileName = TEST_FILE_NAME + "-" + UUID.randomUUID().toString();

        writer = mock(Writer.class);
        csvWriter = spy(new CSVWriterKeepAlive(writer));
        bufferedWriter = spy(new BufferedWriter(writer));

        when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriter);
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenReturn(bufferedWriter);
    }

    @AfterEach
    void tearDown() throws Exception {
        File testFile = new File(uniqueFileName);

        if (testFile.exists()) {
            new File(uniqueFileName).delete();
        }

        writer.close();
    }

    @Test
    void givenValidWriter_whenWriteHeaders_thenWriterWritesHeaders() throws Exception {
        underTest.writeHeadersToCsvFile(writer);

        verify(writer).write(contains(EXPECTED_HEADER_OUTPUT + "\n"));
        verify(writer, times(1)).flush();
        verify(csvWriter, times(1)).close();
    }

    @Test
    void givenValidWriter_whenWriteBeans_thenCsvFileIsCreated() throws Exception {
        underTest.writeBeansWithWriter(writer, Collections.singletonList(TEST_OUTPUT_DATA));

        verify(writer).write(getExpectedDataString() + "\n");
        verify(writer, times(1)).flush();
        verify(csvWriter, times(1)).close();
    }

    @Test
    void givenExceptionOnClose_whenWriteHeaders_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(csvWriter).close();

        assertThrows(ParserException.class, () -> underTest.writeHeadersToCsvFile(writer));

        verify(writer, never()).flush();
    }

    @Test
    void givenExceptionOnClose_whenWriteBeans_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(csvWriter).close();

        assertThrows(ParserException.class, () -> underTest.writeBeansWithWriter(writer, List.of(TEST_OUTPUT_DATA)));

        verify(writer, never()).flush();
    }

    @Test
    void givenExceptionOnClose_whenWriteBeanToCsv_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(bufferedWriter).close();

        assertThrows(ParserException.class, () -> underTest.writeBeansAsCsvFile(uniqueFileName, List.of(TEST_OUTPUT_DATA)));

        // One flush for write header and one flush for write line. Final flush for writer close not called due to exception.
        verify(writer, times(2)).flush();
    }

    @Test
    void givenExceptionOnWrite_whenWriteHeaders_thenExceptionCaughtAndWriterIsClosed() throws Exception {
        try (CSVWriterThrowExceptionStub csvWriterThrowsException = spy(new CSVWriterThrowExceptionStub(writer))) {
            when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriterThrowsException);

            assertThrows(RuntimeException.class, () -> underTest.writeHeadersToCsvFile(writer));

            verify(csvWriterThrowsException, times(1)).close();
        }
    }

    @Test
    void givenExceptionOnWrite_whenWriteBeans_thenExceptionCaughtAndWriterIsClosed() throws Exception {
        try (CSVWriterThrowExceptionStub csvWriterThrowsException = spy(new CSVWriterThrowExceptionStub(writer))) {
            when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriterThrowsException);

            assertThrows(RuntimeException.class, () -> underTest.writeBeansWithWriter(writer, List.of(TEST_OUTPUT_DATA)));

            verify(csvWriterThrowsException, times(1)).close();
        }
    }

    @Test
    void givenExceptionOnWrite_whenWriteBeanToCsv_thenExceptionCaughtAndWriterIsClosed() throws Exception {
        try (CSVWriterThrowExceptionStub csvWriterThrowsException = spy(new CSVWriterThrowExceptionStub(writer))) {
            when(writerWrapper.getCsvWriter(any())).thenReturn(csvWriterThrowsException);

            assertThrows(RuntimeException.class, () -> underTest.writeBeansAsCsvFile(uniqueFileName, List.of(TEST_OUTPUT_DATA)));

            verify(csvWriterThrowsException, times(1)).close();
        }
    }

    @Test
    void givenValidFilePathAndBeans_whenWriteBeanToCsv_thenCsvFileIsCreated() throws Exception {
        when(writerWrapper.getCsvWriter(any())).thenCallRealMethod();
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenCallRealMethod();

        underTest.writeBeansAsCsvFile(uniqueFileName, Collections.singletonList(TEST_OUTPUT_DATA));

        try (InputStream fileInputStream = Files.newInputStream(Paths.get(uniqueFileName))) {

            List<String> dataAsString = new ReaderUtil().readBytesAsStrings(fileInputStream.readAllBytes());

            String expectedDataRow = getExpectedDataString();

            // Remove generated fields (prefixed with non-word characters) from mutation test if exists
            String actualHeader = dataAsString.get(0).replaceAll(",\"\\W.*\"", "");

            assertEquals(EXPECTED_HEADER_OUTPUT, actualHeader, "Header row did not match expected.");
            assertEquals(expectedDataRow, dataAsString.get(1), "Data row did not match expected.");
        }
    }

    @Test
    void givenInvalidFilePath_whenWriteBeanToCsv_thenParserExceptionIsThrown() throws Exception {
        when(writerWrapper.getCsvWriter(any())).thenCallRealMethod();
        when(writerWrapper.getBufferedWriter(any(Path.class))).thenCallRealMethod();

        assertThrows(ParserException.class, () -> underTest.writeBeansAsCsvFile("/", List.of()));
    }

    private String wrapStringInQuotes(String inputString) {
        return "\"" + inputString + "\"";
    }

    private String getExpectedDataString() {
        // Everything will be wrapped in quotes to prevent data json string commas from affecting actual data
        return String.join(",",
            wrapStringInQuotes(""),
            wrapStringInQuotes("notificationId"),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes(""),
            wrapStringInQuotes("createdTimestamp"),
            wrapStringInQuotes(""),
            wrapStringInQuotes("")
        );
    }
}
