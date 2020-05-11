package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.JsonlWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NEWLINE_DELIMITER;

@Component
public class CoreCaseDataJsonlWriterComponentImpl implements JsonlWriterComponent<OutputCoreCaseData> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void writeLinesAsJsonl(Writer writer, List<OutputCoreCaseData> outputBeans) {
        try {
            for (OutputCoreCaseData outputCoreCaseData : outputBeans) {
                writer.write(objectMapper.writeValueAsString(outputCoreCaseData));
                writer.write(NEWLINE_DELIMITER);
            }
        } catch (IOException e) {
            throw new ParserException("Unable to parse or write CoreCaseData as Json Line.", e);
        }
    }
}
