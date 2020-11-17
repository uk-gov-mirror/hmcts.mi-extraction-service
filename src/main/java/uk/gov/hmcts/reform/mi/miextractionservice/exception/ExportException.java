package uk.gov.hmcts.reform.mi.miextractionservice.exception;

public class ExportException extends RuntimeException {

    public static final long serialVersionUID = 8999L;

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable exception) {
        super(message, exception);
    }
}
