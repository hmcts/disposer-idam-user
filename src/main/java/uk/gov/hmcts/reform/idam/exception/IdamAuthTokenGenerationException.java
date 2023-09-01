package uk.gov.hmcts.reform.idam.exception;

public class IdamAuthTokenGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IdamAuthTokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdamAuthTokenGenerationException(String message) {
        super(message);
    }
}
