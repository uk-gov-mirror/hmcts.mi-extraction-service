package uk.gov.hmcts.reform.mi.miextractionservice.component.archive;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
class EncryptArchiveComponentImplTest {

    private static final String TEST_ARCHIVE_PASSWORD = "testPassword";
    private static final String TEST_FILE_NAME = "testFile";
    private static final String TEST_ZIP_NAME = "testArchive";

    private EncryptArchiveComponentImpl classToTest;

    @BeforeEach
    void setUp() {
        classToTest = new EncryptArchiveComponentImpl(TEST_ARCHIVE_PASSWORD);
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
    void givenInputFilesAndOutputPath_whenCreateEncryptedArchive_thenCreateZip() throws Exception {
        Files.createFile(Paths.get(TEST_FILE_NAME));

        classToTest.createArchive(Collections.singletonList(TEST_FILE_NAME), TEST_ZIP_NAME);

        try (ZipFile testZip = new ZipFile(TEST_ZIP_NAME)) {
            assertTrue(
                testZip.isEncrypted(),
                "Zip file should be encrypted."
            );

            try {
                testZip.extractFile(TEST_FILE_NAME, ".");
                fail("Should not reach this point. The method call before this line should have thrown an exception.");
            } catch (ZipException e) {
                assertTrue(
                    e.getMessage().contains("empty or null password"),
                    "Error message should be returned for missing password."
                );
            }
        }

        assertDoesNotThrow(() -> new ZipFile(TEST_ZIP_NAME, TEST_ARCHIVE_PASSWORD.toCharArray())
                               .extractFile(TEST_FILE_NAME, "."),
                           "Should be able to extract zip file with correct password.");
    }

    @Test
    void givenNoFilesToZip_whenCreateEncryptedArchive_thenThrowArchiveException() {
        List<String> emptyList = Collections.emptyList();
        assertThrows(ArchiveException.class, () -> classToTest.createArchive(emptyList, TEST_ZIP_NAME),
                     "ArchiveException should be thrown when nothing to archive.");
    }

    @Test
    void givenPasswordNotSet_whenCreateEncryptedArchive_thenThrowArchiveException() {
        classToTest = new EncryptArchiveComponentImpl("");

        List<String> testList = Collections.singletonList("/");
        assertThrows(ArchiveException.class, () -> classToTest.createArchive(testList, TEST_ZIP_NAME),
                     "ArchiveException should be thrown when no archive password is set.");
    }
}
