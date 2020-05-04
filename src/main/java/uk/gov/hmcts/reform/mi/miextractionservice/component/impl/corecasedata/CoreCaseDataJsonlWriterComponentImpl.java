package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.corecasedata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.JsonlWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
public class CoreCaseDataJsonlWriterComponentImpl implements JsonlWriterComponent<OutputCoreCaseData> {

    @Autowired
    private WriterWrapper writerWrapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void writeLinesAsJsonl(Writer writer, List<OutputCoreCaseData> outputBeans) {
        try (BufferedWriter lineWriter = writerWrapper.getBufferedWriter(writer)) {
            for (OutputCoreCaseData outputCoreCaseData : outputBeans) {
                lineWriter.write(objectMapper.writeValueAsString(outputCoreCaseData));
                lineWriter.newLine();
            }
        } catch (IOException e) {
            throw new ParserException("Unable to parse CoreCaseData as JsonLine.", e);
        }
    }
}
