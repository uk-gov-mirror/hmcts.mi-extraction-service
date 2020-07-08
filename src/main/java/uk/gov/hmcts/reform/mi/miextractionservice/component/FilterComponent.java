package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.time.OffsetDateTime;
import java.util.List;

public interface FilterComponent<T> {

    List<String> filterDataInDateRange(List<String> data, OffsetDateTime fromDate, OffsetDateTime toDate);
}
