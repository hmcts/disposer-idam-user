package uk.gov.hmcts.reform.idam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.service.IdamUserRestorerService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationExecutor implements ApplicationRunner {

    @Value("${service.enabled}")
    private boolean isDisposerEnabled;

    @Value("${service.restorer_enabled}")
    private boolean isRestorerEnabled;

    private final IdamUserDisposerService disposerService;
    private final IdamUserRestorerService restorerService;

    @Override
    public void run(ApplicationArguments args) {
        if (isDisposerEnabled) {
            log.info("Starting the Idam-Disposer job...");
            disposerService.run();
            log.info("Idam-Disposer job has finished!");
        } else if (isRestorerEnabled) {
            log.info("Starting the Idam-Restorer job...");
            restorerService.run();
            log.info("Idam-Restorer job has completed!");
        } else {
            log.info("Not running any Idam-Disposer job as all are disabled...");
        }
    }

}
