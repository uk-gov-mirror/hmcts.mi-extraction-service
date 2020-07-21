package uk.gov.hmcts.reform.mi.miextractionservice.domain;

public final class MiExtractionServiceConstants {

    public static final long TIME_TO_EXPIRY = 24L; // In hours.

    public static final String COMMA_DELIMITER = ",";
    public static final String DOT_DELIMITER = ".";
    public static final String NAME_DELIMITER = "-";
    public static final String QUERY_PART_DELIMITER = "?";
    public static final String NEWLINE_DELIMITER = "\n";

    public static final String CCD_OUTPUT_CONTAINER_NAME = "ccd";
    public static final String CCD_DATA_CONTAINER_PREFIX = CCD_OUTPUT_CONTAINER_NAME + NAME_DELIMITER;

    public static final String CCD_WORKING_FILE_NAME = "CCD_EXTRACT.jsonl";
    public static final String CCD_WORKING_ARCHIVE = "CCD_EXTRACT.zip";

    public static final String NOTIFY_OUTPUT_CONTAINER_NAME = "notify";
    public static final String NOTIFY_CONTAINER_PREFIX = NOTIFY_OUTPUT_CONTAINER_NAME + NAME_DELIMITER;

    public static final String NOTIFY_WORKING_FILE_NAME = "NOTIFY_EXTRACT.jsonl";
    public static final String NOTIFY_WORKING_ARCHIVE = "NOTIFY_EXTRACT.zip";

    public static final String NOTIFY_BLOB_URL_TEMPLATE_PARAMETER = "blobUrl";
    public static final String NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER = "validPeriod";
    public static final String NOTIFY_EMAIL_REFERENCE = "Management Information Extraction Service";

    public static final String NO_FILTER_VALUE = "all";
    public static final String CORE_CASE_DATA_FILTER_VALUE = "CoreCaseData";
    public static final String NOTIFY_FILTER_VALUE = "Notify";

    public static final String NOTIFY_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";

    public static final String PAYMENT_HISTORY_NAME = "payment-history";
    public static final String PAYMENT_ALLOCATION_NAME = "payment-allocation";
    public static final String PAYMENT_REMISSION_NAME = "payment-remission";
    public static final String PAYMENT_FEE_NAME = "payment-fee";

    public static final String EXTRACT_FILE_NAME_SUFFIX = "-extract.jsonl";

    private MiExtractionServiceConstants() {
        // Private Constructor
    }
}
