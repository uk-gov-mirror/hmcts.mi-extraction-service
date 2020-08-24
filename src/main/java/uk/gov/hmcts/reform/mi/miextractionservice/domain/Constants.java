package uk.gov.hmcts.reform.mi.miextractionservice.domain;

public final class Constants {

    // Delimiters
    public static final String COMMA_DELIMITER = ",";
    public static final String DASH_DELIMITER = "-";
    public static final String NEWLINE_DELIMITER = "\n";

    // Date Constants
    public static final String YEAR_MONTH_FORMAT = "yyyy-MM";

    // File Constants
    public static final String JSONL_EXTENSION = ".jsonl";
    public static final String GZIP_EXTENSION = ".gz";
    public static final String ZIP_EXTENSION = ".zip";

    // Email Constants
    public static final long EMAIL_TIME_TO_EXPIRY = 24L; // In hours.
    public static final String EMAIL_SUBJECT = "Management Information Exported Data Url";
    public static final String NOTIFY_BLOB_URL_TEMPLATE_PARAMETER = "blobUrl";
    public static final String NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER = "validPeriod";
    public static final String NOTIFY_EMAIL_REFERENCE = "Management Information Extraction Service";

    private Constants() {
        // Private Constructor
    }
}
