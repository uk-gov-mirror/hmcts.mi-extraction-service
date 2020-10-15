package uk.gov.hmcts.reform.mi.miextractionservice.component.encryption;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRing;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.PostConstruct;

import static org.bouncycastle.bcpg.CompressionAlgorithmTags.ZIP;
import static org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags.AES_256;
import static org.bouncycastle.openpgp.PGPUtil.getDecoderStream;
import static uk.gov.hmcts.reform.mi.miextractionservice.domain.Constants.ENCODED_FILE_EXTENSION;

@Slf4j
@Component
public class PgpEncryptionComponentImpl {

    private static final int BUFFER_SIZE = 1024;
    private static final String TMP_FILE_EXT = ".tmp";
    private final String encryptionPublicKey;
    private final boolean enabled;
    private final FileUtil fileUtil;
    private PGPPublicKey pgpPublicKey;

    public PgpEncryptionComponentImpl(@Value("${sftp.file.encryption.publicKey}") String encryptionPublicKey,
                                      @Value("${sftp.file.encryption.enabled:false}") boolean enabled,
                                      FileUtil fileUtil) {
        this.enabled = enabled;
        this.fileUtil = fileUtil;
        this.encryptionPublicKey = encryptionPublicKey;
    }

    @PostConstruct
    public void load() {
        if (enabled) {
            pgpPublicKey = this.loadPublicKey(encryptionPublicKey.getBytes());
        }
    }

    public String encryptDataToFile(String inputFile) {
        if (enabled) {
            return encryptDataAndReturnFileName(inputFile);
        } else {
            log.info("File encryption not enabled");
            return inputFile;
        }
    }

    /**
     * Returns raw key bytes as a Bouncy Castle PGP public key.
     */
    public PGPPublicKey loadPublicKey(byte[] data) {
        try (InputStream decoderStream = getDecoderStream(new ByteArrayInputStream(data))) {
            BcPGPPublicKeyRing publicKeyRing = getBcPgpPublicKeyRing(decoderStream);
            return lookupPublicSubkey(
                publicKeyRing
            ).orElseThrow(() -> new UnableToLoadPgpPublicKeyException("Unable to load public key"));
        } catch (IOException e) {
            log.error("Error loading public key", e);
            throw new UnableToLoadPgpPublicKeyException(e);
        }
    }

    BcPGPPublicKeyRing getBcPgpPublicKeyRing(InputStream decoderStream) throws IOException {
        return new BcPGPPublicKeyRing(decoderStream);
    }

    /**
     * Return appropriate key or subkey for given task from public key.
     * Weirder older PGP public keys will actually have multiple keys. The main key will usually
     * be sign-only in such situations. So you've gotta go digging in through the key packets and
     * make sure you get the one that's valid for encryption.
     */
    private Optional<PGPPublicKey> lookupPublicSubkey(PGPPublicKeyRing ring) {
        Iterator<PGPPublicKey> keys = ring.getPublicKeys();
        while (keys.hasNext()) {
            PGPPublicKey key = keys.next();
            if (key.isEncryptionKey()) {
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    private PGPEncryptedDataGenerator prepareDataEncryptor(PGPPublicKey pgpPublicKey) {
        BcPGPDataEncryptorBuilder dataEncryptor = new BcPGPDataEncryptorBuilder(AES_256);
        dataEncryptor.setWithIntegrityPacket(true);
        dataEncryptor.setSecureRandom(new SecureRandom());

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
        encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(pgpPublicKey));
        return encryptedDataGenerator;
    }


    private String encryptDataAndReturnFileName(String fileName) {

        Security.addProvider(new BouncyCastleProvider());

        //Creates an empty file in the default temporary-file directory
        String tmpFileName = fileName + TMP_FILE_EXT;
        File inputFile = new File(fileName);
        PGPCompressedDataGenerator pgpCompressedDataGenerator = new PGPCompressedDataGenerator(ZIP);

        try (OutputStream byteArrayOutputStream = fileUtil.newOutputStream(tmpFileName);
             OutputStream out = pgpCompressedDataGenerator.open(byteArrayOutputStream)) {
            PGPUtil.writeFileToLiteralData(
                out,
                PGPLiteralData.BINARY,
                inputFile
            );
        } catch (IOException e) {
            throw new UnableToPgpEncryptZipFileException(e);
        }

        String encodedFileName = fileName + ENCODED_FILE_EXTENSION;
        PGPEncryptedDataGenerator encryptedDataGenerator = prepareDataEncryptor(pgpPublicKey);
        try (InputStream inputStream = fileUtil.newInputStream(tmpFileName);
             OutputStream outputStream = encryptedDataGenerator
                 .open(Files.newOutputStream(Paths.get(encodedFileName)), new byte[BUFFER_SIZE])) {
            IOUtils.copy(inputStream, outputStream, BUFFER_SIZE);
        } catch (IOException | PGPException e) {
            throw new UnableToPgpEncryptZipFileException(e);
        }
        return encodedFileName;
    }

}
