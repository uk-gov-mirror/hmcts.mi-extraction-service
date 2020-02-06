package uk.gov.hmcts.reform.mi.miextractionservice.domain;

@SuppressWarnings("PMD.FieldNamingConventions")
public final class TestConstants {

    public static String CORE_CASE_DATA_JSON_STRING = "{"
        + "\"extraction_date\":\"%s\","
        + "\"case_metadata_event_id\":%s,"
        + "\"ce_case_data_id\":%s,"
        + "\"ce_created_date\":%s,"
        + "\"ce_case_type_id\":\"%s\","
        + "\"ce_case_type_version\":%s,"
        + "\"ce_state_id\":\"%s\","
        + "\"ce_data\":%s"
        + "}";

    public static String TEST_EXTRACTION_DATE = "19700101-0000";
    public static String TEST_CASE_METADATA_EVENT_ID = "1";
    public static String TEST_CASE_DATA_ID = "1";
    public static String TEST_CREATED_DATE = "0";
    public static String TEST_CREATED_DATE_FORMATTED = "1970-01-01 00:00:00.000";
    public static String TEST_CASE_TYPE_ID = "CASETYPE";
    public static String TEST_CASE_TYPE_VERSION = "1";
    public static String TEST_CASE_STATE_ID = "CASESTATE";
    public static String TEST_DATA_JSON_STRING = "{\"hello\":\"world\"}";

    public static String DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING = String.format(CORE_CASE_DATA_JSON_STRING,
        TEST_EXTRACTION_DATE,
        TEST_CASE_METADATA_EVENT_ID,
        TEST_CASE_DATA_ID,
        TEST_CREATED_DATE,
        TEST_CASE_TYPE_ID,
        TEST_CASE_TYPE_VERSION,
        TEST_CASE_STATE_ID,
        TEST_DATA_JSON_STRING);

    private TestConstants() {
        // Private Constructor
    }
}
