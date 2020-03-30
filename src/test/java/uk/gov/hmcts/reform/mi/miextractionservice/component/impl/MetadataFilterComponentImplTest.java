package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class MetadataFilterComponentImplTest {

    private static final String ASSERTION_MESSAGE = "Metadata filter did not return correct boolean.";

    private static final String FILTER_KEY = "dataSource";
    private static final String DEFAULT_FILTER_VALUE = "all";
    private static final String METADATA_SOURCE_KEY = "md_source_id";
    private static final String CORE_CASE_DATA_VALUE = "CoreCaseData";

    private MetadataFilterComponentImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new MetadataFilterComponentImpl();

        ReflectionTestUtils.setField(underTest, FILTER_KEY, CORE_CASE_DATA_VALUE);
    }

    @Test
    public void givenMetadataDataSourceIsDefaultValue_whenFilterBySource_thenReturnTrue() {
        ReflectionTestUtils.setField(underTest, FILTER_KEY, DEFAULT_FILTER_VALUE);

        assertTrue(underTest.filterByMetadata(Collections.singletonMap(METADATA_SOURCE_KEY, CORE_CASE_DATA_VALUE)), ASSERTION_MESSAGE);
    }

    @Test
    public void givenMetadataDataSourceDoesNotMatchFilter_whenFilterBySource_thenReturnFalse() {
        assertFalse(underTest.filterByMetadata(Collections.singletonMap(METADATA_SOURCE_KEY, "NoSet")), ASSERTION_MESSAGE);
    }

    @Test
    public void givenMetadataDataSourceMatchesFilter_whenFilterBySource_thenReturnTrue() {
        assertTrue(underTest.filterByMetadata(Collections.singletonMap(METADATA_SOURCE_KEY, CORE_CASE_DATA_VALUE)), ASSERTION_MESSAGE);
    }

    @Test
    public void givenMetadataDataSourceIsMissing_whenFilterBySource_thenReturnFalse() {
        assertFalse(underTest.filterByMetadata(Collections.singletonMap("Key", CORE_CASE_DATA_VALUE)), ASSERTION_MESSAGE);
    }

    @Test
    public void givenMetadataIsNull_whenFilterBySource_thenReturnFalse() {
        assertFalse(underTest.filterByMetadata(null), ASSERTION_MESSAGE);
    }
}
