package uk.gov.hmcts.reform.mi.miextractionservice.domain;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Builder;
import lombok.Value;

@SuppressWarnings({"checkstyle:MemberName","PMD.FieldNamingConventions","PMD.TooManyFields"})
@Builder
@Value
public class OutputCoreCaseData {

    @CsvBindByPosition(position = 0)
    private String extraction_date;

    @CsvBindByPosition(position = 1)
    private String case_metadata_event_id;

    @CsvBindByPosition(position = 2)
    private String ce_case_data_id;

    @CsvBindByPosition(position = 3)
    private String ce_created_date;

    @CsvBindByPosition(position = 4)
    private String ce_case_type_id;

    @CsvBindByPosition(position = 5)
    private String ce_case_type_version;

    @CsvBindByPosition(position = 6)
    private String ce_state_id;

    @CsvBindByPosition(position = 7)
    String ce_state_name;

    @CsvBindByPosition(position = 8)
    String ce_summary;

    @CsvBindByPosition(position = 9)
    String ce_description;

    @CsvBindByPosition(position = 10)
    String ce_event_id;

    @CsvBindByPosition(position = 11)
    String ce_event_name;

    @CsvBindByPosition(position = 12)
    String ce_user_id;

    @CsvBindByPosition(position = 13)
    String ce_user_first_name;

    @CsvBindByPosition(position = 14)
    String ce_user_last_name;

    @CsvBindByPosition(position = 15)
    private String data;
}
