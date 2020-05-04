package uk.gov.hmcts.reform.mi.miextractionservice.domain;

public final class MiExtractionServiceConstants {

    public static final long TIME_TO_EXPIRY = 24L; // In hours.

    public static final String COMMA_DELIMITER = ",";
    public static final String NAME_DELIMITER = "-";
    public static final String QUERY_PART_DELIMITER = "?";

    public static final String CCD_OUTPUT_CONTAINER_NAME = "ccd";
    public static final String CCD_DATA_CONTAINER_PREFIX = CCD_OUTPUT_CONTAINER_NAME + NAME_DELIMITER;
    public static final String CCD_WORKING_FILE_NAME = "CCD_EXTRACT.jsonl";
    public static final String CCD_WORKING_ARCHIVE = "CCD_EXTRACT.zip";

    public static final String NOTIFY_BLOB_URL_TEMPLATE_PARAMETER = "blobUrl";
    public static final String NOTIFY_VALID_PERIOD_TEMPLATE_PARAMETER = "validPeriod";
    public static final String NOTIFY_EMAIL_REFERENCE = "Management Information Extraction Service";

    public static final String NO_FILTER_VALUE = "all";

    private MiExtractionServiceConstants() {
        // Private Constructor
    }
}
