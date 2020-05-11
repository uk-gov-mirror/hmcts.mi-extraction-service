package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.io.Writer;
import java.util.List;

public interface JsonlWriterComponent<T> {

    void writeLinesAsJsonl(Writer writer, List<T> outputBeans);
}
