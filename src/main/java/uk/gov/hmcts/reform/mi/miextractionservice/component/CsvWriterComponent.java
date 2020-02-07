package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.util.List;

public interface CsvWriterComponent<T> {

    void writeBeansAsCsvFile(String filePath, List<T> outputBeans);
}
