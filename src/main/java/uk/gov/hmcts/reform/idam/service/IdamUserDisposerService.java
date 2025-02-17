package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.util.ListUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamUserDisposerService {

    private final StaleUsersService staleUsersService;
    private final UserRoleService userRoleService;
    private final DeleteUserService deleteUserService;
    private final ParameterResolver parameterResolver;
    private final ListUtils listUtils;
    private final Clock clock;

    public List<String> run() {
        LocalDateTime applicationStartTime = LocalDateTime.now(clock);
        LocalTime cutOffTime = parameterResolver.getCutOffTime();

        // Check if we need to add one day to the cut-off time.
        // Typical run is at night, starting few hours before midnight and running until morning.
        // If the app restarts after the midnight (e.g. k8s restart), but before the cut-off time,
        // then we know that the finish time is on the same day. If it starts after the cut-off time,
        // usually in the evening, then we need to add one day to the cut-off time.
        int dayOffset = applicationStartTime.toLocalTime().isAfter(cutOffTime) ? 1 : 0;
        LocalDateTime cutOff = LocalDateTime.of(applicationStartTime.plusDays(dayOffset).toLocalDate(), cutOffTime);

        List<String> allRemovedStaleUserIds = new ArrayList<>();
        int requestLimit = parameterResolver.getRequestLimit();

        while (requestLimit > 0 && !isCutOffTimeReached(cutOff)) {
            List<String> batchStaleUserIds = staleUsersService.fetchStaleUsers();

            for (List<String> batch : listUtils.partition(batchStaleUserIds, parameterResolver.getRasBatchSize())) {
                List<String> filteredBatch = userRoleService.filterUsersWithRoles(batch);
                if (!filteredBatch.isEmpty()) {
                    log.info("Stale users that have been passed for deletion: {}", filteredBatch);
                    deleteUserService.deleteUsers(filteredBatch);
                    allRemovedStaleUserIds.addAll(filteredBatch);
                }
            }

            if (staleUsersService.hasFinished()) {
                break;
            }
            requestLimit--;
        }

        return allRemovedStaleUserIds;
    }

    private boolean isCutOffTimeReached(LocalDateTime cutOff) {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean afterCutOff = now.isAfter(cutOff);

        if (afterCutOff) {
            log.info("Current time ({}) is after cut off time {}, stopping ...", now, cutOff);
        }
        return afterCutOff;
    }
}
