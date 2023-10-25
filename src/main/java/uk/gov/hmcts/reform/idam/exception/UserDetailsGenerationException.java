package uk.gov.hmcts.reform.idam.exception;

public class UserDetailsGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserDetailsGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserDetailsGenerationException(final String message) {
        super(message);
    }
}
