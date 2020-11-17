package uk.gov.hmcts.reform.mi.miextractionservice.util;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtils {

    public static void deleteFile(String fileName) {
        try {
            if (Files.exists(Paths.get(fileName))) {
                Files.delete(Paths.get(fileName));
            }
        } catch (IOException e) {
            throw new ExportException("Unable to delete file.", e);
        }
    }

    public static BufferedWriter openBufferedWriter(String fileName) throws IOException {
        return Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8);
    }

    public static BufferedReader openReaderFromStream(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private FileUtils() {
        // Private Constructor
    }
}
