package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;

import java.time.OffsetDateTime;
import java.util.Collections;

@SuppressWarnings({"PMD.FieldNamingConventions","PMD.TooManyFields"})
public final class TestConstants {

    public static String CORE_CASE_DATA_JSON_STRING = "{"
        + "\"extraction_date\":\"%s\","
        + "\"ce_id\":%s,"
        + "\"ce_case_data_id\":%s,"
        + "\"ce_created_date\":\"%s\","
        + "\"ce_case_type_id\":\"%s\","
        + "\"ce_case_type_version\":%s,"
        + "\"ce_state_id\":\"%s\","
        + "\"ce_data\":%s"
        + "}";

    public static final String TEST_EXTRACTION_DATE = "19700101-0000";
    public static final String TEST_CASE_METADATA_EVENT_ID = "1";
    public static final String TEST_CASE_DATA_ID = "1";
    public static final String TEST_CREATED_DATE = "1970-01-01T00:00Z";
    public static final  OffsetDateTime TEST_DATE_TIME = OffsetDateTime.parse("2020-01-01T01:00:00.612Z");
    public static final String TEST_CASE_TYPE_ID = "CASETYPE";
    public static final String TEST_CASE_TYPE_VERSION = "1";
    public static final String TEST_CASE_STATE_ID = "CASESTATE";
    public static final String TEST_DATA_JSON_STRING = "{\"hello\":\"world\"}";

    public static final String CCD_EXTRACTION_DATE = "19991201-1010";
    public static final long CCD_DATA_ID = 100_001L;
    public static final String CCD_STATE_ID = "StateId";

    public static String DEFAULT_TEST_CORE_CASE_DATA_JSON_STRING = String.format(CORE_CASE_DATA_JSON_STRING,
        TEST_EXTRACTION_DATE,
        TEST_CASE_METADATA_EVENT_ID,
        TEST_CASE_DATA_ID,
        TEST_CREATED_DATE,
        TEST_CASE_TYPE_ID,
        TEST_CASE_TYPE_VERSION,
        TEST_CASE_STATE_ID,
        TEST_DATA_JSON_STRING);

    public static final String TEMPLATE = "{ \"extraction_date\": \"19991201-1010\", \"ce_id\": 1000001,"
        + " \"ce_case_data_id\": 100001, \"ce_created_date\": \"%s\", \"ce_case_type_id\": \"%s\", \"ce_case_type_version\": 1001,"
        + " \"ce_state_id\": \"StateId\", \"ce_data\": {} }";

    public static final String TEST_CCD_JSONL = String.format(TEMPLATE, "2000-01-29T00:00:00Z", TEST_CASE_TYPE_ID);
    public static final String TEST_CCD_JSONL_OUTDATED_FUTURE = String.format(TEMPLATE, "2001-05-18T01:53:20Z", TEST_CASE_TYPE_ID);
    public static final String TEST_CCD_JSONL_OUTDATED_PAST = String.format(TEMPLATE, "1998-07-11T07:06:40Z", TEST_CASE_TYPE_ID);
    public static final String TEST_CCD_JSONL_NEW_CASETYPE = String.format(TEMPLATE, "2000-06-29T00:00:00.612Z", "NEWCASETYPE");

    public static final CoreCaseData TEST_CCD_JSONL_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .ceId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(OffsetDateTime.parse("2000-01-29T00:00:00Z"))
        .ceCaseTypeId(TEST_CASE_TYPE_ID)
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .ceId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(OffsetDateTime.parse("2001-05-18T01:53:20Z"))
        .ceCaseTypeId(TEST_CASE_TYPE_ID)
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .ceId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(OffsetDateTime.parse("1998-07-11T07:06:40Z"))
        .ceCaseTypeId(TEST_CASE_TYPE_ID)
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_NEW_CASETYPE_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .ceId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(OffsetDateTime.parse("2000-06-29T00:00:00.612Z"))
        .ceCaseTypeId("NEWCASETYPE")
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    private TestConstants() {
        // Private Constructor
    }

}
