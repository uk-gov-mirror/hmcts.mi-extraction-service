package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.component.DataParserComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

@Component
public class NotifyParserComponentImpl implements DataParserComponent<NotificationOutput> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public NotificationOutput parse(String data) {
        try {
            return objectMapper.readValue(data, NotificationOutput.class);
        } catch (JsonProcessingException e) {
            throw new ParserException("Unable to map given data to NotificationOutput.", e);
        }
    }
}
