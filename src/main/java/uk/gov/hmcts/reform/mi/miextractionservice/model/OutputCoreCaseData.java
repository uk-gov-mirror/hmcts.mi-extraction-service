package uk.gov.hmcts.reform.mi.miextractionservice.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Builder;
import lombok.Value;

@SuppressWarnings({"checkstyle:MemberName","PMD.FieldNamingConventions"})
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
    private String data;
}
