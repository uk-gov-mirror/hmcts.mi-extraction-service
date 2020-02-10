package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.model.OutputCoreCaseData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvWriterComponentImpl implements CsvWriterComponent<OutputCoreCaseData> {

    @SuppressWarnings("unchecked")
    @Override
    public void writeBeansAsCsvFile(String filePath, List<OutputCoreCaseData> outputBeans) {
        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(Paths.get(filePath)))) {

            // Note: order of column headers is determined by order of declaration in the class model.
            String[] columnHeaders = getHeaders();

            writer.writeNext(columnHeaders);

            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer)
                .build();

            beanToCsv.write(outputBeans);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new ParserException("Unable to convert output data to CSV format.", e);
        }
    }

    private String[] getHeaders() {
        List<String> headers = new ArrayList<>();

        for (Field field : FieldUtils.getAllFields(OutputCoreCaseData.class)) {
            headers.add(field.getName());
        }

        return headers.toArray(new String[0]);
    }
}
