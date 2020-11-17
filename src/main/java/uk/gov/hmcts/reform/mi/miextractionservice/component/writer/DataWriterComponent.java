package uk.gov.hmcts.reform.mi.miextractionservice.component.writer;

import com.azure.storage.blob.BlobClient;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceProperties;

import java.io.BufferedWriter;
import java.time.LocalDate;

public interface DataWriterComponent {

    int writeRecordsForDateRange(BufferedWriter bufferedWriter,
                                 BlobClient blobClient,
                                 SourceProperties sourceProperties,
                                 LocalDate fromDate,
                                 LocalDate toDate);
}
