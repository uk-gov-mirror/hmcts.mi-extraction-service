package uk.gov.hmcts.reform.mi.miextractionservice.data;

public final class TestConstants {

    // Generated static UUID
    private static final String TEST_ID = "1c9640ba-d3e4-4cdb-bf04-36f00e751189";

    public static final String DASH_DELIMITER = "-";

    public static final String TEST_CONTAINER_NAME = "exp-testcontainer";
    public static final String TEST_BLOB_NAME = "testblob-" + TEST_ID + "-1970-01";

    public static final String EXPORT_CONTAINER_NAME = "exp";
    public static final String TEST_EXPORT_BLOB = "exp-1970-01-01-1970-01-02.zip";

    public static final String TEST_JSONL = "{\"extraction_date\":\"19700101-1010\",\"date_updated\":\"1970-01-01T10:00:00.000Z\"}";

    private TestConstants() {
        // Private constructor
    }
}
