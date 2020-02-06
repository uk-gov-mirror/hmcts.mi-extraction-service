package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.CoreCaseDataFormatterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;
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
            return new OutputCoreCaseData(
                coreCaseData.getExtractionDate(),
                String.valueOf(coreCaseData.getCaseMetadataEventId()),
                String.valueOf(coreCaseData.getCeCaseDataId()),
                dateTimeUtil.getTimestampFormatFromLong(coreCaseData.getCeCreatedDate()),
                coreCaseData.getCeCaseTypeId(),
                String.valueOf(coreCaseData.getCeCaseTypeVersion()),
                coreCaseData.getCeStateId(),
                objectMapper.writeValueAsString(coreCaseData.getCeData())
            );
        } catch (JsonProcessingException e) {
            throw new ParserException("Unable to format given CoreCaseData to output format", e);
        }
    }
}
