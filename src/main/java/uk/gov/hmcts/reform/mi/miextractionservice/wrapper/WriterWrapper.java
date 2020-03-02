package uk.gov.hmcts.reform.mi.miextractionservice.wrapper;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.lib.CSVWriterKeepAlive;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class WriterWrapper {

    public CSVWriterKeepAlive getCsvWriter(Writer writer) {
        return new CSVWriterKeepAlive(writer);
    }

    public BufferedWriter getBufferedWriter(Path filePath) throws IOException {
        return Files.newBufferedWriter(filePath);
    }
}
