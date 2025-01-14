package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamUserDisposerService implements ApplicationListener<ApplicationStartedEvent> {

    private LocalDateTime applicationStartTime;
    private LocalDateTime runBefore;

    private final StaleUsersService staleUsersService;
    private final UserRoleService userRoleService;
    private final DeleteUserService deleteUserService;
    private final ParameterResolver parameterResolver;
    private final Clock clock;

    public List<String> run() {
        LocalTime cutOffTime = parameterResolver.getRunBefore();
        runBefore = LocalDateTime.of(applicationStartTime.plusDays(1).toLocalDate(), cutOffTime);
        List<String> allRemovedStaleUserIds = new ArrayList<>();
        int requestLimit = parameterResolver.getRequestLimit();

        while (requestLimit > 0 && isAllowedToRunTime()) {
            List<String> batchStaleUserIds = staleUsersService.fetchStaleUsers();
            batchStaleUserIds = userRoleService.filterUsersWithRoles(batchStaleUserIds);
            deleteUserService.deleteUsers(batchStaleUserIds);

            if (!batchStaleUserIds.isEmpty()) {
                log.info("Stale users that have been passed for deletion: {}", batchStaleUserIds);
            }

            allRemovedStaleUserIds.addAll(batchStaleUserIds);

            if (staleUsersService.hasFinished()) {
                break;
            }
            requestLimit--;
        }

        return allRemovedStaleUserIds;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        applicationStartTime = LocalDateTime.now(clock);
    }

    private boolean isAllowedToRunTime() {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean inTimeWindow = now.isBefore(runBefore);

        if (!inTimeWindow) {
            log.info("Current time ({}) is after cut off time {}, stopping ...", now, runBefore);
        }
        return inTimeWindow;
    }
}
