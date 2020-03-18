package uk.gov.hmcts.reform.mi.miextractionservice.component;

import java.util.List;

public interface ArchiveComponent {

    void createArchive(List<String> inputPaths, String outputPath);
}
