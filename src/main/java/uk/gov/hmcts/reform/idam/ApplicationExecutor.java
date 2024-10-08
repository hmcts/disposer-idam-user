package uk.gov.hmcts.reform.idam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.DeleteUserService;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.service.StaleUsersService;
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;


@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationExecutor implements ApplicationRunner {

    @Value("${service.enabled}")
    private boolean isDisposerEnabled;

    private final IdamUserDisposerService disposerService;
    private final SecurityUtil securityUtil;
    private final LoggingSummaryUtils loggingSummaryUtils;
    private final StaleUsersService staleUsersService;
    private final DeleteUserService deleteUserService;

    @Override
    public void run(ApplicationArguments args) {
        if (isDisposerEnabled) {
            execute();
        } else {
            log.info("Not running any Idam-Disposer job as all are disabled...");
        }
    }

    private void execute() {
        long disposerStartTime = System.currentTimeMillis();
        try {
            log.info("Starting the Idam-Disposer job...");
            securityUtil.generateTokens();
            disposerService.run();
            log.info("Idam-Disposer job has finished!");
        } catch (Exception e) {
            //This specific message error has been added in Azure log to look for these traces in alert
            // query and create alert if disposer-idam-user throw any exception because of any reason.
            log.error("Error executing Disposer Idam User service : " +  e);
            //To have stack trace of this exception as we are catching the exception
            // stack trace will not be logged by azure
            log.error("Error executing Disposer Idam User service", e);
        } finally {
            loggingSummaryUtils.logSummary(
                disposerStartTime,
                System.currentTimeMillis(),
                staleUsersService.getTotalStaleUsers(),
                deleteUserService.getSuccessfulDeletions(),
                deleteUserService.getFailedDeletions()
            );
        }
    }
}
