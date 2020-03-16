package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.ArchiveComponent;
import uk.gov.hmcts.reform.mi.miextractionservice.exception.ArchiveException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "archive.encryption", name = "enabled", havingValue = "false")
public class ArchiveComponentImpl implements ArchiveComponent {

    @Override
    public void createArchive(List<String> inputPaths, String outputPath) {

        try {
            List<File> filesList = new ArrayList<>();
            inputPaths.forEach(path -> filesList.add(new File(path)));

            new ZipFile(outputPath).addFiles(filesList);
        } catch (ZipException e) {
            throw new ArchiveException("Failure to create new archive.", e);
        }
    }
}
