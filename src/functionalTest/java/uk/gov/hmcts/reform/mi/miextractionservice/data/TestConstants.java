package uk.gov.hmcts.reform.mi.miextractionservice.data;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public final class TestConstants {

    public static final String TEST_CONTAINER_NAME = "test-testcontainer";
    public static final String TEST_EXTRACTION_CONTAINER_NAME = "extraction-test";
    public static final String TEST_FILTER_CONTAINER_NAME = "filter-test";
    public static final String TEST_BLOB_NAME = "testblob-1970-01";

    public static final String EXPORT_CONTAINER_NAME = "test";
    public static final String EXPORT_EXTRACTION_CONTAINER_NAME = "extraction-test";
    public static final String EXPORT_FILTER_CONTAINER_NAME = "filter-test";
    public static final String TEST_EXPORT_BLOB = "test-1970-01-01-1970-01-02.zip";
    public static final String TEST_EXTRACTION_EXPORT_BLOB = "extraction-test-1970-01-01-1970-01-02.zip";
    public static final String TEST_FILTER_EXPORT_BLOB = "filter-test-1970-01-01-1970-01-02.zip";

    public static final String EXTRACT_FILE_NAME = "test-1970-01-01-1970-01-02.jsonl.gz";
    public static final String EXTRACT_FILTER_FILE_NAME = "filter-test-1970-01-01-1970-01-02.jsonl.gz";

    public static final String TEST_JSONL = "{\"extraction_date\":\"19700101-1010\",\"date_updated\":\"1970-01-01T10:00:00.000Z\","
                                            + "\"hello\":\"world's\"}";

    public static final String TEST_FILTER_PASS_JSONL = "{\"extraction_date\":\"19700101-1010\",\"test\":\"pass\"}";
    public static final String TEST_FILTER_FAIL_JSONL = "{\"extraction_date\":\"19700101-1010\",\"test\":\"fail\"}";

    private TestConstants() {
        // Private constructor
    }
}
