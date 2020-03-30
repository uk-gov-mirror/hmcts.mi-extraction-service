package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.MetadataFilterComponent;

import java.util.Map;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NO_FILTER_VALUE;

@Component
public class MetadataFilterComponentImpl implements MetadataFilterComponent {

    private static final String METADATA_SOURCE_KEY = "md_source_id";

    @Value("${filter.data-source}")
    private String dataSource;

    @Override
    public boolean filterByMetadata(Map<String, String> metadata) {
        return dataSource.equalsIgnoreCase(NO_FILTER_VALUE)
            || metadata != null
                && metadata.containsKey(METADATA_SOURCE_KEY)
                && metadata.get(METADATA_SOURCE_KEY).equalsIgnoreCase(dataSource);
    }
}