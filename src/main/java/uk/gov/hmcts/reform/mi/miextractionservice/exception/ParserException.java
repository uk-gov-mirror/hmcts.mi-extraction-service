package uk.gov.hmcts.reform.mi.miextractionservice.exception;

public class ParserException extends RuntimeException {

    public static final long serialVersionUID = 125L;

    public ParserException(String message, Throwable exception) {
        super(message, exception);
    }
}
