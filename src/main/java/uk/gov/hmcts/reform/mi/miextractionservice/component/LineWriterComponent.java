package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.io.Writer;
import java.util.List;

public interface LineWriterComponent {

    void writeLines(Writer writer, List<String> data);
}
