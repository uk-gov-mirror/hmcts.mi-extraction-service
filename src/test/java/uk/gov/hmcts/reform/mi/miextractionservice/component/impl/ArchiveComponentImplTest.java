package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ArchiveComponentImplTest {

    private static final String TEST_FILE_NAME = "test";
    private static final String TEST_ZIP_NAME = "output";

    private String uniqueFileName;
    private String uniqueZipName;

    private ArchiveComponentImpl underTest;

    @BeforeEach
    void setUp() {
        uniqueFileName = TEST_FILE_NAME + UUID.randomUUID().toString();
        uniqueZipName = TEST_ZIP_NAME + UUID.randomUUID().toString();

        underTest = new ArchiveComponentImpl();
    }

    @AfterEach
    void tearDown() {
        File testFile = new File(uniqueFileName);
        File testZip = new File(uniqueZipName);

        if (testFile.exists()) {
            new File(uniqueFileName).delete();
        }

        if (testZip.exists()) {
            new File(uniqueZipName).delete();
        }
    }

    @Test
    void givenInputFilesAndOutputPath_whenCreateEncryptedArchive_thenCreateZip() throws Exception {
        File testFile = new File(uniqueFileName);
        testFile.createNewFile();

        underTest.createArchive(Collections.singletonList(uniqueFileName), uniqueZipName);

        ZipFile testZip = new ZipFile(uniqueZipName);

        testZip.extractFile(uniqueFileName, ".");

        assertDoesNotThrow(() -> new ZipFile(uniqueZipName).extractFile(uniqueFileName, "."),
            "Unexpected exception was thrown.");
    }

    @Test
    void givenNoFilesToZip_whenCreateEncryptedArchive_thenThrowArchiveException() {
        assertThrows(ArchiveException.class, () -> underTest.createArchive(List.of(), uniqueZipName),
            "Expected exception was not thrown for missing files.");
    }
}
