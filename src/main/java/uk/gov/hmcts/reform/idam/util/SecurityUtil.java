package uk.gov.hmcts.reform.idam.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.exception.ServiceAuthTokenGenerationException;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityUtil {
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    private String idamClientToken;
    private String serviceAuthToken;

    public String getIdamClientToken() {
        return idamClientToken;
    }

    public String getServiceAuthorization() {
        return serviceAuthToken;
    }


    @Scheduled(fixedRate = 55, timeUnit = TimeUnit.MINUTES)
    public void generateTokens() {
        generateIdamToken();
        generateServiceToken();
    }

    private void generateServiceToken() {
        try {
            serviceAuthToken = authTokenGenerator.generate();
        } catch (final Exception exception) {
            String msg = String.format(
                    "Unable to generate service auth token due to error - %s", exception.getMessage());
            log.error(msg, exception);
            throw new ServiceAuthTokenGenerationException(msg, exception);
        }
    }

    private void generateIdamToken() {
        try {
            idamClientToken = idamClient.getAccessToken(null, null);
        } catch (final Exception exception) {
            String msg = String.format("Unable to generate IDAM token due to error - %s", exception.getMessage());
            log.error(msg, exception);
            throw new IdamAuthTokenGenerationException(msg, exception);
        }

    }
}
