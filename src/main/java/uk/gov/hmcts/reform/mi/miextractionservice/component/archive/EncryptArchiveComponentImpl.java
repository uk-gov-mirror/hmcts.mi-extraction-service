package uk.gov.hmcts.reform.mi.miextractionservice.component.archive;

import lombok.AllArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
@ConditionalOnProperty(prefix = "archive.encryption", name = "enabled", havingValue = "true")
public class EncryptArchiveComponentImpl implements ArchiveComponent {

    private final @Value("${archive.encryption.password}") String archivePassword;

    @Override
    public void createArchive(List<String> inputPaths, String outputPath) {

        if (StringUtils.isEmpty(archivePassword)) {
            throw new ArchiveException("Archive password for encryption is not set.");
        }

        try {
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

            List<File> filesList = inputPaths.stream().map(File::new).collect(Collectors.toList());

            new ZipFile(outputPath, archivePassword.toCharArray()).addFiles(filesList, zipParameters);
        } catch (ZipException e) {
            throw new ArchiveException("Failure to create or encrypt new archive.", e);
        }
    }
}
