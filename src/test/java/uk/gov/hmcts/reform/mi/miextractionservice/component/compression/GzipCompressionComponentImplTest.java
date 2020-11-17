package uk.gov.hmcts.reform.mi.miextractionservice.component.compression;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ExportException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class GzipCompressionComponentImplTest {

    private static final String TEST_FILE_NAME = "testFile";
    private static final String TEST_GZIP_NAME = "testGzip";

    private GzipCompressionComponentImpl classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new GzipCompressionComponentImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (Files.exists(Paths.get(TEST_FILE_NAME))) {
            Files.delete(Paths.get(TEST_FILE_NAME));
        }

        if (Files.exists(Paths.get(TEST_GZIP_NAME))) {
            Files.delete(Paths.get(TEST_GZIP_NAME));
        }
    }

    @Test
    void givenInputFileAndOutputPath_whenCompressFile_thenCreateGzipFile() throws Exception {
        Files.createFile(Paths.get(TEST_FILE_NAME));

        classToTest.compressFile(TEST_FILE_NAME, TEST_GZIP_NAME);

        assertTrue(new File(TEST_GZIP_NAME).exists(), "Gzip file should be created.");
    }

    @Test
    void givenInvalidInputFile_whenCompressFile_thenThrowExportException() {
        assertThrows(ExportException.class, () -> classToTest.compressFile("DoesNotExist", TEST_GZIP_NAME));
    }
}
