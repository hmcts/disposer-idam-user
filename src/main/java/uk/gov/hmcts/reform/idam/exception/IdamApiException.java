package uk.gov.hmcts.reform.idam.exception;

public class IdamApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IdamApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdamApiException(String message) {
        super(message);
    }
}
