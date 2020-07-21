package uk.gov.hmcts.reform.mi.miextractionservice.component;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import java.io.BufferedWriter;
import java.time.OffsetDateTime;
import java.util.List;

public interface WriteDataComponent {

    int writeData(BufferedWriter writer, List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate, SourceEnum source);
}
