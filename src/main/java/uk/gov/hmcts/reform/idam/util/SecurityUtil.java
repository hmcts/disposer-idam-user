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

    @Scheduled(fixedRate = 55, timeUnit = TimeUnit.MINUTES)
    public void generateTokens() {
        idamTokenGenerator.generateIdamToken();
        idamTokenGenerator.generateRoleAssignmentIdamToken();
        serviceTokenGenerator.generateServiceToken();
    }
}
