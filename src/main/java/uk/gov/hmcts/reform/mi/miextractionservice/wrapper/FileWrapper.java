package uk.gov.hmcts.reform.mi.miextractionservice.wrapper;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileWrapper {

    public void deleteFileOnExit(String filePath) {
        new File(filePath).deleteOnExit();
    }
}
