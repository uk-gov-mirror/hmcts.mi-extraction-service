package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.LineWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static uk.gov.hmcts.reform.mi.miextractionservice.domain.MiExtractionServiceConstants.NEWLINE_DELIMITER;

@Component
public class LineWriterComponentImpl implements LineWriterComponent {

    @Override
    public void writeLines(Writer writer, List<String> data) {
        try {
            for (String line : data) {
                writer.write(line);
                writer.write(NEWLINE_DELIMITER);
            }
        } catch (IOException e) {
            throw new ParserException("Unable to write list of data.", e);
        }
    }
}
