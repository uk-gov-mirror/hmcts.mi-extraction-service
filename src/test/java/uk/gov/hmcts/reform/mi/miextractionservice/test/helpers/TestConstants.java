package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;

import java.util.Collections;

@SuppressWarnings({"PMD.FieldNamingConventions","PMD.TooManyFields"})
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

    public static final String TEST_EXTRACTION_DATE = "19700101-0000";
    public static final String TEST_CASE_METADATA_EVENT_ID = "1";
    public static final String TEST_CASE_DATA_ID = "1";
    public static final String TEST_CREATED_DATE = "0";
    public static final String TEST_CREATED_DATE_FORMATTED = "1970-01-01 00:00:00.000";
    public static final String TEST_CASE_TYPE_ID = "CASETYPE";
    public static final String TEST_CASE_TYPE_VERSION = "1";
    public static final String TEST_CASE_STATE_ID = "CASESTATE";
    public static final String TEST_CASE_STATE_NAME = "ce_state_name";
    public static final String TEST_SUMMARY = "ce_summary";
    public static final String TEST_DESCRIPTION = "ce_description";
    public static final String TEST_EVENT_ID = "ce_event_id";
    public static final String TEST_EVENT_NAME = "ce_event_name";
    public static final String TEST_USER_ID = "ce_user_id";
    public static final String TEST_USER_FIRST_NAME  = "ce_user_first_name";
    public static final String TEST_USER_LAST_NAME = "ce_user_last_name";
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

    public static final String TEMPLATE = "{ \"extraction_date\": \"19991201-1010\", \"case_metadata_event_id\": 1000001,"
        + " \"ce_case_data_id\": 100001, \"ce_created_date\": %s, \"ce_case_type_id\": \"%s\", \"ce_case_type_version\": 1001,"
        + " \"ce_state_id\": \"StateId\", \"ce_data\": {} }";

    public static final String TEST_CCD_JSONL = String.format(TEMPLATE, "949104000000", TEST_CASE_TYPE_ID);
    public static final String TEST_CCD_JSONL_OUTDATED_FUTURE = String.format(TEMPLATE, "990150800000", TEST_CASE_TYPE_ID);
    public static final String TEST_CCD_JSONL_OUTDATED_PAST = String.format(TEMPLATE, "900140800000", TEST_CASE_TYPE_ID);
    public static final String TEST_CCD_JSONL_NEW_CASETYPE = String.format(TEMPLATE, "949104000000", "NEWCASETYPE");

    public static final CoreCaseData TEST_CCD_JSONL_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .caseMetadataEventId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(949_104_000_000L)
        .ceCaseTypeId(TEST_CASE_TYPE_ID)
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .caseMetadataEventId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(990_150_800_000L)
        .ceCaseTypeId(TEST_CASE_TYPE_ID)
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .caseMetadataEventId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(900_140_800_000L)
        .ceCaseTypeId(TEST_CASE_TYPE_ID)
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_NEW_CASETYPE_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate(CCD_EXTRACTION_DATE)
        .caseMetadataEventId(1_000_001L)
        .ceCaseDataId(CCD_DATA_ID)
        .ceCreatedDate(949_104_000_000L)
        .ceCaseTypeId("NEWCASETYPE")
        .ceCaseTypeVersion(1001L)
        .ceStateId(CCD_STATE_ID)
        .ceData(Collections.emptyMap())
        .build();

    public static final OutputCoreCaseData TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA = OutputCoreCaseData.builder()
        .extraction_date(CCD_EXTRACTION_DATE)
        .case_metadata_event_id("1000001")
        .ce_case_data_id("100001")
        .ce_created_date("2000-01-29 00:00:00.000")
        .ce_case_type_id(TEST_CASE_TYPE_ID)
        .ce_case_type_version("1001")
        .ce_state_id(CCD_STATE_ID)
        .data("{}")
        .build();

    private TestConstants() {
        // Private Constructor
    }
}
