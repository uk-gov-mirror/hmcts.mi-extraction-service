package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
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

@ExtendWith(SpringExtension.class)
class NotifyJsonlWriterComponentImplTest {

    private static final NotificationOutput TEST_OUTPUT_DATA = NotificationOutput.builder()
        .extractionDate("20000101-1000")
        .id("uuid")
        .createdAt("2000-01-01")
        .build();
    private static final List<NotificationOutput> TEST_OUTPUT_LIST = Collections.singletonList(TEST_OUTPUT_DATA);

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotifyJsonlWriterComponentImpl underTest;

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
        underTest.writeLinesAsJsonl(writer, Collections.singletonList(TEST_OUTPUT_DATA));

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
