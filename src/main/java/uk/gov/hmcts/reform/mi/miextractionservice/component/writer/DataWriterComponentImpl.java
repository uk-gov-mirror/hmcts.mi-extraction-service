package uk.gov.hmcts.reform.mi.miextractionservice.component.writer;

import com.azure.storage.blob.BlobClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.filter.DateFilterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.component.parser.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.mi.miextractionservice.util.FileUtils.openReaderFromStream;

@AllArgsConstructor
@Component
public class DataWriterComponentImpl implements DataWriterComponent {

    private final DataParserComponent dataParserComponent;
    private final DateFilterComponent dateFilterComponent;

    @Override
    public int writeRecordsForDateRange(BufferedWriter bufferedWriter,
                                        BlobClient blobClient,
                                        SourceProperties sourceProperties,
                                        LocalDate fromDate,
                                        LocalDate toDate) {

        int recordsCount = 0;

        try (InputStream inputStream = blobClient.openInputStream();
             BufferedReader bufferedReader = openReaderFromStream(inputStream)) {

            String line = bufferedReader.readLine();

            while (line != null) {
                if (StringUtils.isNotEmpty(line)) {
                    JsonNode jsonNode = dataParserComponent.parseJsonString(line);
                    if (dateFilterComponent.filterByDate(jsonNode, sourceProperties, fromDate, toDate)) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        recordsCount++;
                    }
                }

                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new ExportException("Unable to read or write data from blob.", e);
        }

        return recordsCount;
    }
}
