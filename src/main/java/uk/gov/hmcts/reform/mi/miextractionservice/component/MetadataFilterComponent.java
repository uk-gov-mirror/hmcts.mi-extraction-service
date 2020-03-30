package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.util.Map;

public interface MetadataFilterComponent {

    boolean filterByMetadata(Map<String, String> metadata);
}
