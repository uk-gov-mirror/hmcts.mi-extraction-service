package uk.gov.hmcts.reform.mi.miextractionservice.data;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public final class TestConstants {

    // Generated static UUID
    private static final String TEST_ID = "a934d7e9-7726-4e09-95e1-157371c51811";

    public static final String LOCALHOST_IP = "127.0.0.1";

    public static final String TEST_CONTAINER_NAME = "ccd-testcontainer-" + TEST_ID;
    public static final String TEST_BLOB_NAME = "testblob-" + TEST_ID + "-1970-01";

    public static final String CCD_EXPORT_CONTAINER_NAME = "ccd";
    public static final String TEST_EXPORT_BLOB = "1970-01-01-1970-01-02-CCD_EXTRACT.zip";

    public static final String TEST_CCD_JSONL = "{\"extraction_date\":\"19991201-1010\","
        + "\"ce_id\":1000001,\"ce_case_data_id\":100001,\"ce_created_date\":1001,"
        + "\"ce_case_type_id\":\"CASETYPE\",\"ce_case_type_version\":1001,\"ce_state_id\":\"StateId\",\"ce_data\":{}}";

    // Notify

    public static final String NOTIFY_TEST_CONTAINER_NAME = "notify-testcontainer-" + TEST_ID;
    public static final String NOTIFY_EXPORT_CONTAINER_NAME = "notify";
    public static final String NOTIFY_TEST_EXPORT_BLOB = "1970-01-01-1970-01-02-NOTIFY_EXTRACT.zip";

    public static final String TEST_NOTIFY_JSONL = "{\"extraction_date\":\"19991201-1010\","
        + "\"id\":1000001,\"service\":\"service\",\"reference\":\"reference\","
        + "\"type\":\"type\",\"status\":\"status\",\"created_at\":\"1970-01-01T01:00:00.000000Z\"}";

    public static final String PAYMENT_HISTORY_TEST_CONTAINER_NAME = "payment-history-testcontainer-" + TEST_ID;
    public static final String PAYMENT_ALLOCATION_TEST_CONTAINER_NAME = "payment-allocation-testcontainer-" + TEST_ID;
    public static final String PAYMENT_REMISSION_TEST_CONTAINER_NAME = "payment-remission-testcontainer-" + TEST_ID;
    public static final String PAYMENT_FEE_TEST_CONTAINER_NAME = "payment-fee-testcontainer-" + TEST_ID;
    public static final String PAYMENT_HISTORY_EXPORT_CONTAINER_NAME = "payment-history";
    public static final String PAYMENT_ALLOCATION_EXPORT_CONTAINER_NAME = "payment-allocation";
    public static final String PAYMENT_REMISSION_EXPORT_CONTAINER_NAME = "payment-remission";
    public static final String PAYMENT_FEE_EXPORT_CONTAINER_NAME = "payment-fee";
    public static final String PAYMENT_HISTORY_TEST_EXPORT_BLOB = "1970-01-01-1970-01-02-payment-history-extract.jsonl";
    public static final String PAYMENT_ALLOCATION_TEST_EXPORT_BLOB = "1970-01-01-1970-01-02-payment-allocation-extract.jsonl";
    public static final String PAYMENT_REMISSION_TEST_EXPORT_BLOB = "1970-01-01-1970-01-02-payment-remission-extract.jsonl";
    public static final String PAYMENT_FEE_TEST_EXPORT_BLOB = "1970-01-01-1970-01-02-payment-fee-extract.jsonl";

    public static final String TEST_PAYMENT_JSONL = "{\"extraction_date\":\"19991201-1010\","
        + "\"id\":\"id\",\"date_created\":\"1970-01-01T01:00:00.000000Z\",\"date_updated\":\"1970-01-01T01:00:00.000000Z\","
        + "\"sh_date_updated\":\"1970-01-01T01:00:00.000000Z\"}";

    private TestConstants() {
        // Private constructor
    }
}
