package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.OutputCoreCaseData;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.lib.CSVWriterKeepAlive;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvWriterComponentImpl implements CsvWriterComponent<OutputCoreCaseData> {

    @Autowired
    private WriterWrapper writerWrapper;

    @Override
    public void writeHeadersToCsvFile(Writer writer) {
        try (CSVWriterKeepAlive csvWriter = writerWrapper.getCsvWriter(writer)) {
            // Note: order of column headers is determined by order of declaration in the class model.
            String[] columnHeaders = getHeaders();

            csvWriter.writeNext(columnHeaders);
        } catch (IOException e) {
            throw new ParserException("Unable to write column headers to csv.", e);
        }
    }

    @Override
    public void writeBeansAsCsvFile(String filePath, List<OutputCoreCaseData> outputBeans) {
        try (BufferedWriter bufferedWriter = writerWrapper.getBufferedWriter(Paths.get(filePath))) {
            writeHeadersToCsvFile(bufferedWriter);
            writeBeansWithWriter(bufferedWriter, outputBeans);
        } catch (IOException e) {
            throw new ParserException("Unable to initialise file writer.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeBeansWithWriter(Writer writer, List<OutputCoreCaseData> outputBeans) {
        try (CSVWriterKeepAlive csvWriter = writerWrapper.getCsvWriter(writer)) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(csvWriter).build();

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
