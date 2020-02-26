package uk.gov.hmcts.reform.mi.miextractionservice.lib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Writer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class CsvWriterKeepAliveTest {

    @Test
    public void closingCsvWriterDoesNotCloseInputWriter() throws Exception {
        try (Writer writer = mock(Writer.class); CSVWriterKeepAlive underTest = new CSVWriterKeepAlive(writer)) {
            underTest.close();

            verify(writer, times(1)).flush();
            verify(writer, never()).close();
        }
    }
}
