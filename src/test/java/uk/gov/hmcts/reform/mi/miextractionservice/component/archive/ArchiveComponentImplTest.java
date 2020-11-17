package uk.gov.hmcts.reform.mi.miextractionservice.component.archive;

import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ArchiveComponentImplTest {

    private static final String TEST_FILE_NAME = "testFile";
    private static final String TEST_ZIP_NAME = "testArchive";

    private ArchiveComponentImpl classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new ArchiveComponentImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (Files.exists(Paths.get(TEST_FILE_NAME))) {
            Files.delete(Paths.get(TEST_FILE_NAME));
        }

        if (Files.exists(Paths.get(TEST_ZIP_NAME))) {
            Files.delete(Paths.get(TEST_ZIP_NAME));
        }
    }

    @Test
    void givenInputFilesAndOutputPath_whenCreateArchive_thenCreateZip() throws Exception {
        Files.createFile(Paths.get(TEST_FILE_NAME));

        classToTest.createArchive(Collections.singletonList(TEST_FILE_NAME), TEST_ZIP_NAME);

        assertDoesNotThrow(() -> new ZipFile(TEST_ZIP_NAME).extractFile(TEST_FILE_NAME, "."),
                           "Archive should have extractable file.");
    }

    @Test
    void givenNoFilesToZip_whenCreateArchive_thenThrowArchiveException() {
        List<String> emptyList = Collections.emptyList();
        assertThrows(ArchiveException.class, () -> classToTest.createArchive(emptyList, TEST_ZIP_NAME),
                     "ArchiveException should be thrown when nothing to archive.");
    }
}
