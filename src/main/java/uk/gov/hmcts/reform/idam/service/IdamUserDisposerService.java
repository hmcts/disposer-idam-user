package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamUserDisposerService {

    private final StaleUsersService staleUsersService;
    private final UserRoleService userRoleService;

    public List<String> run() {
        List<String> allRemovedStaleUserIds = new ArrayList<>();
        do {
            List<String> batchStaleUserIds = staleUsersService.fetchStaleUsers();
            batchStaleUserIds = userRoleService.filterUsersWithRoles(batchStaleUserIds);
            batchStaleUserIds.forEach(user -> log.info("Stale users that would be passed to deletion {}", user));
            allRemovedStaleUserIds.addAll(batchStaleUserIds);
        } while (!staleUsersService.hasFinished());
        return allRemovedStaleUserIds;
    }

}
