package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.component.EncryptArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class EncryptArchiveComponentImpl implements EncryptArchiveComponent {

    @Value("${archive.password}")
    private String archivePassword;

    @Override
    public void createEncryptedArchive(List<String> inputPaths, String outputPath) {

        if (StringUtils.isEmpty(archivePassword)) {
            throw new ArchiveException("Archive password for encryption is not set.");
        }

        try {
            List<File> filesList = new ArrayList<>();
            inputPaths.forEach(path -> filesList.add(new File(path)));

            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

            new ZipFile(outputPath, archivePassword.toCharArray()).addFiles(filesList, zipParameters);
        } catch (ZipException e) {
            throw new ArchiveException("Failure to create or encrypt new archive.", e);
        }
    }
}
