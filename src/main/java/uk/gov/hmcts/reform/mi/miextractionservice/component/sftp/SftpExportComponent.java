package uk.gov.hmcts.reform.mi.miextractionservice.component.sftp;

public interface SftpExportComponent {

    void copyFile(String file);

    void copyFile(String file, String source);

    void loadFile(String file, String destinyFilePath);

    void checkConnection();

}
