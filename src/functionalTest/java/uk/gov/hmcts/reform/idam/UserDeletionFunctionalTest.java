package uk.gov.hmcts.reform.idam;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.helpers.IdamUserDataProvider;
import uk.gov.hmcts.reform.idam.helpers.RoleAssignmentProvider;
import uk.gov.hmcts.reform.idam.service.DeleteUserService;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("functional")
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class UserDeletionFunctionalTest {

    @Inject
    private IdamUserDataProvider idamUserDataProvider;

    @Inject
    private RoleAssignmentProvider roleAssignmentProvider;

    @Inject
    private DeleteUserService deleteUserService;

    @Inject
    private IdamUserDisposerService userDisposerService;

    private String userWithRole;
    private final List<String> userWithRoleList = new ArrayList<>();

    @Test
    @DirtiesContext
    void givenStaleUserExistsThenDisposerShouldBeAbleToDeleteThatUser() {
        String userId = idamUserDataProvider.setup();
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).hasSize(1);
        assertThat(deletedStaleUsers.getFirst()).isEqualTo(userId);
    }

    @Test
    @DirtiesContext
    void givenStaleUserHasAmRolesThenDisposerShouldNotDeleteThatUser() {
        userWithRole = idamUserDataProvider.setup();
        roleAssignmentProvider.setup(userWithRole);
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).isEmpty();
    }

    @Test
    @DirtiesContext
    void givenOneStaleUserHasAmRoleAnotherDoesntOneShouldBeDeleted() {
        userWithRole = idamUserDataProvider.setup();
        roleAssignmentProvider.setup(userWithRole);
        String userIdNoRole = idamUserDataProvider.setup();
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).hasSize(1);
        assertThat(deletedStaleUsers.getFirst()).isEqualTo(userIdNoRole);
    }

    @Test
    @DirtiesContext
    void shouldFetchAllRoleAssignmentPages() {
        int numOfUsers = 4;

        for (int i = 0; i < numOfUsers; i++) {
            userWithRoleList.add(idamUserDataProvider.setup());
        }
        log.info("Created users - {}", userWithRoleList);
        roleAssignmentProvider.assignRole(userWithRoleList);
        String userWithoutRole = idamUserDataProvider.setup();
        log.info("User without role {}", userWithoutRole);
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).hasSize(1);
        assertThat(deletedStaleUsers.getFirst()).isEqualTo(userWithoutRole);
    }


    @AfterEach
    public void teardown() {
        if (userWithRole != null) {
            deleteUserService.deleteUser(userWithRole);
            roleAssignmentProvider.deleteRoles(List.of(userWithRole));
        }
        deleteUserService.deleteUsers(userWithRoleList);
        roleAssignmentProvider.deleteRoles(userWithRoleList);
    }
}
