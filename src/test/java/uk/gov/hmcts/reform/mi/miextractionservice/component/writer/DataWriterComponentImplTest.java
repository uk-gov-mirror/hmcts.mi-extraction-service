package uk.gov.hmcts.reform.mi.miextractionservice.component.writer;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.component.filter.DateFilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.parser.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.CloseResource") // Streams are mocked.
@ExtendWith(SpringExtension.class)
class DataWriterComponentImplTest {

    private static final SourceProperties SOURCE_PROPERTIES = SourceProperties.builder().build();
    private static final LocalDate FROM_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2000, 1, 1);

    @Mock
    private DataParserComponent dataParserComponent;
    @Mock
    private DateFilterComponent dateFilterComponent;

    DataWriterComponentImpl classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new DataWriterComponentImpl(dataParserComponent, dateFilterComponent);
    }

    @Test
    void givenBlobClient_whenWriteRecordsForDateRange_writeDataAndReturnCountOfRecordsWithinDateRange() throws Exception {
        final BufferedWriter bufferedWriter = mock(BufferedWriter.class);
        final BlobClient blobClient = mock(BlobClient.class);

        final String data = "inDate\ninDate\n\nnotInDate\ninDate";
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.getBytes());
        final BlobInputStream blobInputStream = mock(BlobInputStream.class, delegatesTo(byteArrayInputStream));

        final JsonNode inDateJsonNode = mock(JsonNode.class);
        final JsonNode notInDateJsonNode = mock(JsonNode.class);

        when(blobClient.openInputStream()).thenReturn(blobInputStream);

        when(dataParserComponent.parseJsonString(eq("inDate"))).thenReturn(inDateJsonNode);
        when(dataParserComponent.parseJsonString(eq("notInDate"))).thenReturn(notInDateJsonNode);

        when(dateFilterComponent.filterByDate(eq(inDateJsonNode), eq(SOURCE_PROPERTIES), eq(FROM_DATE), eq(TO_DATE)))
            .thenReturn(true);
        when(dateFilterComponent.filterByDate(not(eq(inDateJsonNode)), any(), any(), any()))
            .thenReturn(false);

        int actual = classToTest.writeRecordsForDateRange(bufferedWriter, blobClient, SOURCE_PROPERTIES, FROM_DATE, TO_DATE);

        assertEquals(3, actual, "Three records should be written for date range.");

        verify(bufferedWriter, times(3)).write(anyString());
        verify(bufferedWriter, times(3)).newLine();
    }

    @Test
    void givenExceptionOnBlobInputStream_whenWriteRecordsForDateRange_thenThrowExportException() throws IOException {
        final BufferedWriter bufferedWriter = mock(BufferedWriter.class);
        final BlobClient blobClient = mock(BlobClient.class);

        final ByteArrayInputStream byteArrayInputStream = mock(ByteArrayInputStream.class);
        doThrow(IOException.class).when(byteArrayInputStream).close();
        final BlobInputStream blobInputStream = mock(BlobInputStream.class, delegatesTo(byteArrayInputStream));

        when(blobClient.openInputStream()).thenReturn(blobInputStream);

        assertThrows(ExportException.class,
            () -> classToTest.writeRecordsForDateRange(bufferedWriter, blobClient, SOURCE_PROPERTIES, FROM_DATE, TO_DATE));
    }
}
