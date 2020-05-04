package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.CoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

@Component
public class CoreCaseDataParserComponentImpl implements DataParserComponent<CoreCaseData> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public CoreCaseData parse(String data) {
        try {
            return objectMapper.readValue(data, CoreCaseData.class);
        } catch (JsonProcessingException e) {
            throw new ParserException("Unable to map given data to CoreCaseData.", e);
        }
    }
}
