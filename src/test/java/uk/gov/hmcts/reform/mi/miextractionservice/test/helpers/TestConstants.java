package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;

import java.util.Collections;

@SuppressWarnings({"PMD.FieldNamingConventions","PMD.AvoidDuplicateLiterals","PMD.UseUnderscoresInNumericLiterals"})
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

    public static final String TEST_CCD_JSONL = "{\"extraction_date\":\"19991201-1010\","
        + "\"case_metadata_event_id\":1000001,\"ce_case_data_id\":100001,\"ce_created_date\":949104000000,"
        + "\"ce_case_type_id\":\"CASETYPE\",\"ce_case_type_version\":1001,\"ce_state_id\":\"StateId\",\"ce_data\":{}}";
    public static final String TEST_CCD_JSONL_OUTDATED_FUTURE = "{\"extraction_date\":\"19991201-1010\","
        + "\"case_metadata_event_id\":1000001,\"ce_case_data_id\":100001,\"ce_created_date\":990140800000,"
        + "\"ce_case_type_id\":\"CASETYPE\",\"ce_case_type_version\":1001,\"ce_state_id\":\"StateId\",\"ce_data\":{}}";
    public static final String TEST_CCD_JSONL_OUTDATED_PAST = "{\"extraction_date\":\"19991201-1010\","
        + "\"case_metadata_event_id\":1000001,\"ce_case_data_id\":100001,\"ce_created_date\":900140800000,"
        + "\"ce_case_type_id\":\"CASETYPE\",\"ce_case_type_version\":1001,\"ce_state_id\":\"StateId\",\"ce_data\":{}}";

    public static final CoreCaseData TEST_CCD_JSONL_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate("19991201-1010")
        .caseMetadataEventId(1000001L)
        .ceCaseDataId(100001L)
        .ceCreatedDate(949104000000L)
        .ceCaseTypeId("CASETYPE")
        .ceCaseTypeVersion(1001L)
        .ceStateId("StateId")
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_OUTDATED_FUTURE_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate("19991201-1010")
        .caseMetadataEventId(1000001L)
        .ceCaseDataId(100001L)
        .ceCreatedDate(990140800000L)
        .ceCaseTypeId("CASETYPE")
        .ceCaseTypeVersion(1001L)
        .ceStateId("StateId")
        .ceData(Collections.emptyMap())
        .build();

    public static final CoreCaseData TEST_CCD_JSONL_OUTDATED_PAST_AS_CORE_CASE_DATA = CoreCaseData.builder()
        .extractionDate("19991201-1010")
        .caseMetadataEventId(1000001L)
        .ceCaseDataId(100001L)
        .ceCreatedDate(900140800000L)
        .ceCaseTypeId("CASETYPE")
        .ceCaseTypeVersion(1001L)
        .ceStateId("StateId")
        .ceData(Collections.emptyMap())
        .build();

    public static final OutputCoreCaseData TEST_CCD_JSONL_AS_OUTPUT_CORE_CASE_DATA = OutputCoreCaseData.builder()
        .extraction_date("19991201-1010")
        .case_metadata_event_id("1000001")
        .ce_case_data_id("100001")
        .ce_created_date("2000-01-29 00:00:00.000")
        .ce_case_type_id("CASETYPE")
        .ce_case_type_version("1001")
        .ce_state_id("StateId")
        .data("{}")
        .build();

    private TestConstants() {
        // Private Constructor
    }
}
