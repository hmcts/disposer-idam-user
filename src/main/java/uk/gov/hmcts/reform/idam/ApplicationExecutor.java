package uk.gov.hmcts.reform.idam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;


@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationExecutor implements ApplicationRunner {

    @Value("${service.enabled}")
    private boolean isDisposerEnabled;

    private final IdamUserDisposerService disposerService;
    private final SecurityUtil securityUtil;

    @Override
    public void run(ApplicationArguments args) {
        if (isDisposerEnabled) {
            log.info("Starting the Idam-Disposer job...");
            securityUtil.generateTokens();
            disposerService.run();
            log.info("Idam-Disposer job has finished!");
        } else {
            log.info("Not running any Idam-Disposer job as all are disabled...");
        }
    }

}
