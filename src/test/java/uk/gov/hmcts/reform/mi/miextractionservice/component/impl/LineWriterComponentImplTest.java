package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NEWLINE_DELIMITER;

@ExtendWith(SpringExtension.class)
class LineWriterComponentImplTest {

    private static final String TEST_OUTPUT_DATA = "{\"test\":\"json\"}";
    private static final List<String> TEST_OUTPUT_LIST = Collections.singletonList(TEST_OUTPUT_DATA);

    @InjectMocks
    private LineWriterComponentImpl underTest;

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
        underTest.writeLines(writer, TEST_OUTPUT_LIST);

        verify(writer, times(1)).write(TEST_OUTPUT_DATA);
        verify(writer, times(1)).write(NEWLINE_DELIMITER);
    }

    @Test
    void givenExceptionOnClose_whenWriteBeans_thenThrowParserException() throws Exception {
        doThrow(new IOException("Broken close")).when(writer).write(anyString());

        assertThrows(ParserException.class, () -> underTest.writeLines(writer, TEST_OUTPUT_LIST));

        verify(writer, never()).flush();
    }
}
