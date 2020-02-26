package uk.gov.hmcts.reform.mi.miextractionservice.lib;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Modified CSVWriter class to keeps input Writer alive after finishing with the CSVWriter.
 * Note that the input Writer must therefore be closed separately from closing the CSVWriter.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class CSVWriterKeepAlive extends CSVWriter {

    public CSVWriterKeepAlive(Writer writer) {
        super(writer);
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
