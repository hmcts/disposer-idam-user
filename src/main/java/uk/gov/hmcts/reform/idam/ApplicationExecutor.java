package uk.gov.hmcts.reform.idam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationExecutor implements ApplicationRunner {

    @Value("${service.enabled}")
    private boolean isServiceEnabled;

    private final IdamUserDisposerService service;

    @Override
    public void run(ApplicationArguments args) {
        if (isServiceEnabled) {
            log.info("Starting the Idam-Disposer job...");
            service.run();
            log.info("Idam-Disposer job has finished...");
        } else {
            log.info("Not running Idam-Disposer job as it is disabled...");
        }
    }

}
