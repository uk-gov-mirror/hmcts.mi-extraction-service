package uk.gov.hmcts.reform.mi.miextractionservice.exception;

public class ArchiveException extends RuntimeException {

    public static final long serialVersionUID = 123L;

    public ArchiveException(String message) {
        super(message);
    }

    public ArchiveException(String message, Throwable exception) {
        super(message, exception);
    }
}
