package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

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

    public List<String> run() {

        List<String> allRemovedStaleUserIds = new ArrayList<>();
        int requestLimit = parameterResolver.getRequestLimit();

        while (requestLimit > 0) {
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
}
