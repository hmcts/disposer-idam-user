package uk.gov.hmcts.reform.idam.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.exception.ServiceAuthTokenGenerationException;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter
public class ServiceTokenGenerator {

    private final AuthTokenGenerator authTokenGenerator;

    private String serviceAuthToken = "dummy token";

    private static final String SERVICE_TOKEN_ERROR = "Unable to generate service auth token due to error";

    public void generateServiceToken() {
        try {
            serviceAuthToken = authTokenGenerator.generate();
        } catch (final Exception exception) {
            log.error(SERVICE_TOKEN_ERROR, exception);
            throw new ServiceAuthTokenGenerationException(SERVICE_TOKEN_ERROR, exception);
        }
    }
}
