package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.io.Writer;
import java.util.List;

public interface CsvWriterComponent<T> {

    void writeHeadersToCsvFile(Writer writer);

    void writeBeansAsCsvFile(String filePath, List<T> outputBeans);

    void writeBeansWithWriter(Writer writer, List<T> outputBeans);
}
