package uk.gov.hmcts.reform.idam;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.helpers.IdamUserDataProvider;
import uk.gov.hmcts.reform.idam.helpers.RoleAssignmentProvider;
import uk.gov.hmcts.reform.idam.service.DeleteUserService;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
class UserDeletionFunctionalTest {

    @Inject
    private IdamUserDataProvider idamUserDataProvider;

    @Inject
    RoleAssignmentProvider roleAssignmentProvider;

    @Inject
    DeleteUserService deleteUserService;

    @Inject
    private IdamUserDisposerService userDisposerService;

    private String userWithRole;

    @Test
    @DirtiesContext
    void givenStaleUserExistsThenDisposerShouldBeAbleToDeleteThatUser() {
        String userId = idamUserDataProvider.setup();
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).hasSize(1);
        assertThat(deletedStaleUsers.get(0)).isEqualTo(userId);
    }

    @Test
    @DirtiesContext
    void givenStaleUserHasRolesThenDisposerShouldNotDeleteThatUser() {
        userWithRole = idamUserDataProvider.setup();
        roleAssignmentProvider.setup(userWithRole);
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).isEmpty();
    }

    @Test
    @DirtiesContext
    void givenOneStaleUserHasRoleAnotherDoesntOneShouldBeDeleted() {
        userWithRole = idamUserDataProvider.setup();
        roleAssignmentProvider.setup(userWithRole);
        String userIdNoRole = idamUserDataProvider.setup();
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).hasSize(1);
        assertThat(deletedStaleUsers.get(0)).isEqualTo(userIdNoRole);
    }

    @AfterEach
    public void teardown() {
        if (userWithRole != null) {
            deleteUserService.deleteUser(userWithRole);
        }
    }
}
