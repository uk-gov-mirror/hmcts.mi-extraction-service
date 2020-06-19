package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.micore.model.NotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.component.JsonlWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NEWLINE_DELIMITER;

@Component
public class NotifyJsonlWriterComponentImpl implements JsonlWriterComponent<NotificationOutput> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void writeLinesAsJsonl(Writer writer, List<NotificationOutput> outputBeans) {
        try {
            for (NotificationOutput notificationOutput : outputBeans) {
                writer.write(objectMapper.writeValueAsString(notificationOutput));
                writer.write(NEWLINE_DELIMITER);
            }
        } catch (IOException e) {
            throw new ParserException("Unable to parse or write NotificationOutput as Json Line.", e);
        }
    }
}
