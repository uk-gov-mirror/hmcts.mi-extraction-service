package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
class EncryptArchiveComponentImplTest {

    private static final String TEST_FILE_NAME = "test";
    private static final String TEST_ZIP_NAME = "output";

    private static final String TEST_PASSWORD = "testPassword";

    private String uniqueFileName;
    private String uniqueZipName;

    private EncryptArchiveComponentImpl underTest;

    @BeforeEach
    void setUp() {
        uniqueFileName = TEST_FILE_NAME + UUID.randomUUID().toString();
        uniqueZipName = TEST_ZIP_NAME + UUID.randomUUID().toString();

        underTest = new EncryptArchiveComponentImpl();

        ReflectionTestUtils.setField(underTest, "archivePassword", TEST_PASSWORD);
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

        assertTrue(testZip.isEncrypted(), "Zip file was not encrypted.");

        try {
            testZip.extractFile(uniqueFileName, ".");

            fail("Should not reach this point. The method call before this line should have thrown an exception.");
        } catch (ZipException e) {
            assertTrue(e.getMessage().contains("empty or null password"), "Expected error message was not returned.");
        }

        assertDoesNotThrow(() -> new ZipFile(uniqueZipName, TEST_PASSWORD.toCharArray()).extractFile(uniqueFileName, "."),
            "Unexpected exception was thrown.");
    }

    @Test
    void givenNoFilesToZip_whenCreateEncryptedArchive_thenThrowArchiveException() {
        assertThrows(ArchiveException.class, () -> underTest.createArchive(Collections.emptyList(), uniqueZipName),
            "Expected exception was not thrown for missing files.");
    }

    @Test
    void givenPasswordNotSet_whenCreateEncryptedArchive_thenThrowArchiveException() {
        ReflectionTestUtils.setField(underTest, "archivePassword", null);

        assertThrows(ArchiveException.class, () -> underTest.createArchive(Collections.singletonList("/"), uniqueZipName),
            "Expected exception was not thrown for no password.");
    }
}