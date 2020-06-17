package uk.gov.hmcts.reform.mi.miextractionservice.component.impl.notify;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.CsvWriterComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.domain.CsvNotificationOutput;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ParserException;
import uk.gov.hmcts.reform.mi.miextractionservice.lib.CSVWriterKeepAlive;
import uk.gov.hmcts.reform.mi.miextractionservice.wrapper.WriterWrapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.List;

@Component
public class NotifyCsvWriterComponentImpl implements CsvWriterComponent<CsvNotificationOutput> {

    @Autowired
    private WriterWrapper writerWrapper;

    @Override
    public void writeHeadersToCsvFile(Writer writer) {
        try (CSVWriterKeepAlive csvWriter = writerWrapper.getCsvWriter(writer)) {
            String[] headers = {
                "extraction_date", "id", "service", "reference", "email_address", "phone_number", "line_1", "line_2",
                "line_3", "line_4", "line_5", "line_6", "postcode", "type", "status", "template_id", "template_version",
                "template_uri", "template_name", "body", "subject", "created_at", "sent_at", "completed_at"
            };

            csvWriter.writeNext(headers);
        } catch (IOException e) {
            throw new ParserException("Unable to write column headers to csv.", e);
        }
    }

    @Override
    public void writeBeansAsCsvFile(String filePath, List<CsvNotificationOutput> outputBeans) {
        try (BufferedWriter bufferedWriter = writerWrapper.getBufferedWriter(Paths.get(filePath))) {
            writeHeadersToCsvFile(bufferedWriter);
            writeBeansWithWriter(bufferedWriter, outputBeans);
        } catch (IOException e) {
            throw new ParserException("Unable to initialise file writer.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeBeansWithWriter(Writer writer, List<CsvNotificationOutput> outputBeans) {
        try (CSVWriterKeepAlive csvWriter = writerWrapper.getCsvWriter(writer)) {
            StatefulBeanToCsv<CsvNotificationOutput> beanToCsv =
                new StatefulBeanToCsvBuilder<CsvNotificationOutput>(csvWriter).build();

            beanToCsv.write(outputBeans);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new ParserException("Unable to convert output data to CSV format.", e);
        }
    }
}
