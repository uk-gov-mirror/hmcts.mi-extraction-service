package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.util.List;

public interface EncryptArchiveComponent {

    void createEncryptedArchive(List<String> inputPaths, String outputPath);
}
