package uk.gov.hmcts.reform.mi.miextractionservice.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class FileUtil {
    public OutputStream newOutputStream(String fileName) throws IOException {
        return Files.newOutputStream(Paths.get(fileName));
    }

    public InputStream newInputStream(String fileName) throws IOException {
        return Files.newInputStream(Paths.get(fileName));
    }
}
