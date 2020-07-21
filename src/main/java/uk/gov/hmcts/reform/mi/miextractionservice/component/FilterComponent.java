package uk.gov.hmcts.reform.mi.miextractionservice.component;

import uk.gov.hmcts.reform.mi.miextractionservice.domain.SourceEnum;

import java.time.OffsetDateTime;
import java.util.List;

public interface FilterComponent {

    List<String> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate, SourceEnum source);
}
