package uk.gov.hmcts.reform.idam.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class SecurityUtil {

    private final ServiceTokenGenerator serviceTokenGenerator;

    private final IdamTokenGenerator idamTokenGenerator;

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
}
