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
    public void generateTokens() {
        log.info("Security Util Generate token has been called");
        idamTokenGenerator.generateIdamToken();
        idamTokenGenerator.generatePasswordTypeToken();
        serviceTokenGenerator.generateServiceToken();
    }
}
