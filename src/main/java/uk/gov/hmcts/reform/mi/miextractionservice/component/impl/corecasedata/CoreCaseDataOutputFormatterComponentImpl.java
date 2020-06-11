package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.util.DateTimeUtil;

@Component
public class CoreCaseDataOutputFormatterComponentImpl implements CoreCaseDataFormatterComponent<OutputCoreCaseData> {

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OutputCoreCaseData formatData(CoreCaseData coreCaseData) {
        try {
            return OutputCoreCaseData
                .builder()
                .extraction_date(coreCaseData.getExtractionDate())
                .case_metadata_event_id(String.valueOf(coreCaseData.getCaseMetadataEventId()))
                .ce_case_data_id(String.valueOf(coreCaseData.getCeCaseDataId()))
                .ce_created_date(dateTimeUtil.getTimestampFormatFromLong(coreCaseData.getCeCreatedDate()))
                .ce_case_type_id(coreCaseData.getCeCaseTypeId())
                .ce_case_type_version(String.valueOf(coreCaseData.getCeCaseTypeVersion()))
                .ce_state_id(coreCaseData.getCeStateId())
                .ce_state_name(coreCaseData.getCeStateName())
                .ce_summary(coreCaseData.getSummary())
                .ce_description(coreCaseData.getDescription())
                .ce_event_id(coreCaseData.getCeEventId())
                .ce_event_name(coreCaseData.getCeEventName())
                .ce_user_id(coreCaseData.getCeUserId())
                .ce_user_first_name(coreCaseData.getCeUserFirstName())
                .ce_user_last_name(coreCaseData.getCeUserLastName())
                .data(objectMapper.writeValueAsString(coreCaseData.getCeData()))
                .build();
        } catch (JsonProcessingException e) {
            throw new ParserException("Unable to format given CoreCaseData to output format", e);
        }
    }
}
