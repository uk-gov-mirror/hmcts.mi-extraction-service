package uk.gov.hmcts.reform.mi.miextractionservice.component.compression;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.apache.commons.io.IOUtils.copy;

@Component
public class GzipCompressionComponentImpl implements CompressionComponent {

    @Override
    public void compressFile(String fileName, String outputName) {
        try (FileInputStream fileInputStream = new FileInputStream(fileName);
             FileOutputStream fileOutputStream = new FileOutputStream(outputName);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream)) {

            copy(fileInputStream, gzipOutputStream);
        } catch (IOException e) {
            throw new ExportException("Unable to compress output files.", e);
        }
    }
}
