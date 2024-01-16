package uk.gov.hmcts.reform.idam.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@EnableScheduling
public class SecurityUtil {

    private final ServiceTokenGenerator serviceTokenGenerator;

    private final IdamTokenGenerator idamTokenGenerator;

    public SecurityUtil(ServiceTokenGenerator serviceTokenGenerator, IdamTokenGenerator idamTokenGenerator) {
        this.serviceTokenGenerator = serviceTokenGenerator;
        this.idamTokenGenerator = idamTokenGenerator;
    }

    @Scheduled(initialDelay = 55, fixedRate = 55, timeUnit = TimeUnit.MINUTES)
    public void generateIdamTokens() {
        log.info("Security Util Generate Idam token has been called");
        idamTokenGenerator.generateIdamToken();
        idamTokenGenerator.generatePasswordTypeToken();
    }

    @Scheduled(initialDelay = 237, fixedRate = 237, timeUnit = TimeUnit.MINUTES)
    public void generateServiceToken() {
        log.info("Security Util Generate Service token has been called");
        serviceTokenGenerator.generateServiceToken();
    }

    public void generateTokens() {
        generateIdamTokens();
        generateServiceToken();
    }

    public Map<String, String> getAuthHeaders() {
        return Map.of(
            "Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader(),
            "ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken()
        );
    }
}
