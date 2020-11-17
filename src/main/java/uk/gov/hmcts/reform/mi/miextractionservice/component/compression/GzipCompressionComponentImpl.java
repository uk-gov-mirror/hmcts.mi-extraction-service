package uk.gov.hmcts.reform.mi.miextractionservice.component.compression;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import static org.apache.commons.io.IOUtils.copy;

@Component
public class GzipCompressionComponentImpl implements CompressionComponent {

    @Override
    public void compressFile(String fileName, String outputName) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(fileName));
             OutputStream outputStream = Files.newOutputStream(Paths.get(outputName));
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {

            copy(inputStream, gzipOutputStream);
        } catch (IOException e) {
            throw new ExportException("Unable to compress output files.", e);
        }
    }
}
