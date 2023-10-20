package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

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
    private final SecurityUtil securityUtil;
    private final LoggingSummaryUtils loggingSummaryUtils;

    public List<String> run() {
        long disposerStartTime = System.currentTimeMillis();
        securityUtil.generateTokens();
        List<String> allRemovedStaleUserIds = new ArrayList<>();
        int requestLimit = parameterResolver.getRequestLimit();

        while (requestLimit > 0) {
            List<String> batchStaleUserIds = staleUsersService.fetchStaleUsers();
            batchStaleUserIds = userRoleService.filterUsersWithRoles(batchStaleUserIds);
            deleteUserService.deleteUsers(batchStaleUserIds);
            log.info("Stale users that have been passed for deletion: {}", batchStaleUserIds);
            allRemovedStaleUserIds.addAll(batchStaleUserIds);

            if (staleUsersService.hasFinished()) {
                break;
            }

            requestLimit--;
        }
        long disposerEndTime = System.currentTimeMillis();
        loggingSummaryUtils.logSummary(disposerStartTime, disposerEndTime, staleUsersService.getTotalStaleUsers(),
                                       allRemovedStaleUserIds.size());

        return allRemovedStaleUserIds;
    }

}
