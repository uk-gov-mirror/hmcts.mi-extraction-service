package uk.gov.hmcts.reform.mi.miextractionservice.component.archive;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "archive.encryption", name = "enabled", havingValue = "false")
public class ArchiveComponentImpl implements ArchiveComponent {

    @Override
    public void createArchive(List<String> inputPaths, String outputPath) {
        try {
            List<File> filesList = inputPaths.stream().map(File::new).collect(Collectors.toList());

            new ZipFile(outputPath).addFiles(filesList);
        } catch (ZipException e) {
            throw new ArchiveException("Failure to create new archive.", e);
        }
    }
}
