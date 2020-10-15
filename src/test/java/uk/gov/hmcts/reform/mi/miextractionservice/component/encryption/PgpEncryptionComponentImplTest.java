package uk.gov.hmcts.reform.mi.miextractionservice.component.encryption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.mi.miextractionservice.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PgpEncryptionComponentImplTest {

    String dataContent = "Some content";
    String testFilePath = "tmpFile.text.bgc";
    String publicKeyPath = "encryption/pubkey.asc";
    String privateKeyPath = "encryption/privatekey.asc";
    String privateKeyPassword = "Password1";

    String key = "6 14";

    @Spy
    FileUtil fileUtil;
    PgpEncryptionComponentImpl classToTest;

    @Test
    void encryptDataToFile() throws Exception {
        classToTest = new PgpEncryptionComponentImpl(loadKey(), true, fileUtil);
        classToTest.load();
        File in = createTempFile(testFilePath, dataContent.getBytes());
        String outFile = classToTest.encryptDataToFile(in.getName());
        byte[] encryptedBytes = FileUtils.readFileToByteArray(new File(outFile));
        getStreamFromClasspath(privateKeyPath);
        try (InputStream privateKeyStream = getStreamFromClasspath(privateKeyPath)) {
            assertEquals(PgpDecryptionHelper.decryptFile(
                encryptedBytes,
                privateKeyStream,
                privateKeyPassword.toCharArray()
            ), dataContent, "Expected decoded data");

        }
    }

    @Test
    void testEncryptionDisabled() throws Exception {
        classToTest = new PgpEncryptionComponentImpl(loadKey(), false, fileUtil);
        classToTest.load();
        String fileName = "TestFileName";
        String outFile = classToTest.encryptDataToFile(fileName);
        assertEquals(fileName, outFile, "Expected file name");
    }

    @Test
    void testCorruptedKey() {
        classToTest = new PgpEncryptionComponentImpl("randomString", true, fileUtil);
        assertThrows(UnableToLoadPgpPublicKeyException.class, () -> classToTest.load(),
                     "Expected error on broken key"
        );
    }

    @Test
    void testErrorCreatingEncryptedFile() throws Exception {
        classToTest = new PgpEncryptionComponentImpl(loadKey(), true, fileUtil);
        classToTest.load();

        File in = new File(testFilePath);

        when(fileUtil.newOutputStream(in.getName() + ".tmp")).thenThrow(new IOException());
        assertThrows(UnableToPgpEncryptZipFileException.class, () -> classToTest.encryptDataToFile(testFilePath));
    }

    @Test
    void testErrorReadingFile() throws Exception {
        classToTest = new PgpEncryptionComponentImpl(loadKey(), true, fileUtil);
        classToTest.load();

        File in = new File(testFilePath);

        when(fileUtil.newInputStream(in.getName() + ".tmp")).thenThrow(new IOException());
        assertThrows(UnableToPgpEncryptZipFileException.class, () -> classToTest.encryptDataToFile(testFilePath));
    }

    @Test
    void testNotKeyInKeyRing() throws Exception {
        classToTest = spy(new PgpEncryptionComponentImpl("", false, fileUtil));
        classToTest.load();

        BcPGPPublicKeyRing mockBcPgpPublicKeyRing = mock(BcPGPPublicKeyRing.class);
        PGPPublicKey mockPgpPublicKey = mock(PGPPublicKey.class);
        when(mockPgpPublicKey.isEncryptionKey()).thenReturn(false);
        when(mockBcPgpPublicKeyRing.getPublicKeys()).thenReturn(Arrays.asList(mockPgpPublicKey).iterator());
        doReturn(mockBcPgpPublicKeyRing).when(classToTest).getBcPgpPublicKeyRing(any());
        byte[] keyBytes = loadKey().getBytes();
        assertThrows(UnableToLoadPgpPublicKeyException.class, () -> classToTest.loadPublicKey(keyBytes));
    }


    private InputStream getStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    private String loadKey() throws IOException {
        try (InputStream rssStream = getStreamFromClasspath(publicKeyPath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copy(rssStream, outputStream);
            return outputStream.toString();
        }
    }

    private File createTempFile(String fileName, byte[] inputFile) throws IOException {
        try (OutputStream fos = Files.newOutputStream(Paths.get(fileName))) {
            fos.write(inputFile);
            return new File(fileName);
        }
    }

}
