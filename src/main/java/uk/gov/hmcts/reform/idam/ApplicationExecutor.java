package uk.gov.hmcts.reform.idam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.StaleUsersService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationExecutor implements ApplicationRunner {


    @Value("${service.enabled}")
    private boolean serviceEnabled;

    private final StaleUsersService service;

    @Override
    public void run(ApplicationArguments args) {
        if (serviceEnabled) {
            List<String> staleUsers = service.retrieveStaleUsers();
            staleUsers = service.filterUsersWithRoleAssignments(staleUsers);
            staleUsers.forEach(user -> log.info("Stale user without roles %s", user));
        }
    }

}
