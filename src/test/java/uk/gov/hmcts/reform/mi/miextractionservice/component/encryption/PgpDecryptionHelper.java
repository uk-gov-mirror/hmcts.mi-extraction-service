package uk.gov.hmcts.reform.mi.miextractionservice.component.encryption;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Iterator;

public final class PgpDecryptionHelper {
    // Prevent instantiation.
    private PgpDecryptionHelper() {
    }


    private static PGPEncryptedDataList getPgpEncryptedDataList(byte[] in) throws IOException {
        PGPObjectFactory objectFactory = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
        Object encryptedDataObject = objectFactory.nextObject();

        // the first object might be a PGP marker packet.
        if (encryptedDataObject instanceof PGPEncryptedDataList) {
            return (PGPEncryptedDataList) encryptedDataObject;
        } else {
            return (PGPEncryptedDataList) objectFactory.nextObject();
        }
    }

    /**
     * decrypt the passed in message stream.
     */
    @SuppressWarnings("unchecked")
    public static String decryptFile(byte[] in, InputStream keyIn, char... passwd) throws IOException, PGPException {

        Security.addProvider(new BouncyCastleProvider());

        PGPEncryptedDataList encryptedDataList = getPgpEncryptedDataList(in);

        // find the secret key
        Iterator<PGPPublicKeyEncryptedData> it = encryptedDataList.getEncryptedDataObjects();
        PGPPrivateKey pgpPrivateKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (pgpPrivateKey == null && it.hasNext()) {
            pbe = it.next();

            pgpPrivateKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd);
        }

        if (pgpPrivateKey == null) {
            throw new IllegalArgumentException("Secret key for message not found.");
        }
        return decryptMessage(getPgpLiteralData(pbe, pgpPrivateKey), pbe);
    }

    private static PGPLiteralData getPgpLiteralData(PGPPublicKeyEncryptedData pbe, PGPPrivateKey pgpPrivateKey)
        throws PGPException, IOException {
        Object message;

        try (InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(pgpPrivateKey))) {

            PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());

            message = plainFact.nextObject();

            if (message instanceof PGPCompressedData) {
                PGPCompressedData compressedData = (PGPCompressedData) message;
                PGPObjectFactory pgpFact =
                    new PGPObjectFactory(
                        compressedData.getDataStream(),
                        new JcaKeyFingerprintCalculator()
                    );

                message = pgpFact.nextObject();
            }
        }
        if (message instanceof PGPLiteralData) {
            return (PGPLiteralData) message;
        } else if (message instanceof PGPOnePassSignatureList) {
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }

    }

    private static String decryptMessage(PGPLiteralData literalData, PGPPublicKeyEncryptedData pbe)
        throws IOException, PGPException {
        try (InputStream unc = literalData.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            IOUtils.copy(unc, byteArrayOutputStream);

            if (pbe.isIntegrityProtected() && !pbe.verify()) {
                throw new PGPException("Message failed integrity check");
            }

            return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        }
    }

    private static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyId, char... pass)
        throws IOException, PGPException {
        PGPSecretKeyRingCollection pgpSec =
            new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn),
                new JcaKeyFingerprintCalculator()
            );
        return findPrivateKey(pgpSec.getSecretKey(keyId), pass);
    }

    private static PGPPrivateKey findPrivateKey(PGPSecretKey pgpSecKey, char... pass)
        throws PGPException {
        if (pgpSecKey == null) {
            return null;
        }

        PBESecretKeyDecryptor decryptor =
            new BcPBESecretKeyDecryptorBuilder(
                new BcPGPDigestCalculatorProvider()
            ).build(pass);
        return pgpSecKey.extractPrivateKey(decryptor);
    }
}
