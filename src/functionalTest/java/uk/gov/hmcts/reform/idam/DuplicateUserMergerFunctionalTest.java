package uk.gov.hmcts.reform.idam;


import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.helpers.IdamUserDataProvider;
import uk.gov.hmcts.reform.idam.helpers.LauDeletionLogEntryProvider;
import uk.gov.hmcts.reform.idam.helpers.RoleAssignmentProvider;
import uk.gov.hmcts.reform.idam.requests.RequestedRole;
import uk.gov.hmcts.reform.idam.requests.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.idam.requests.RoleAssignmentsAssignRoleRequest;
import uk.gov.hmcts.reform.idam.requests.RoleRequest;
import uk.gov.hmcts.reform.idam.response.RoleAssignment;
import uk.gov.hmcts.reform.idam.response.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.service.IdamDuplicateUserMergerService;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
@Disabled("Missing search-user scope")
class DuplicateUserMergerFunctionalTest {

    private static final String CLAIMANT = "[CLAIMANT]";
    private static final String CREATOR = "[CREATOR]";

    @Inject
    IdamDuplicateUserMergerService idamDuplicateUserMergerService;

    @Inject
    LauDeletionLogEntryProvider lauDeletionLogEntryProvider;

    @Inject
    IdamUserDataProvider idamUserDataProvider;

    @Inject
    RoleAssignmentProvider roleAssignmentProvider;

    @Test
    @DirtiesContext
    void givenDeletedUserHasDifferentActiveUserOnTheSameEmailItShouldMergeRoleAssignments() {
        String userId = UUID.randomUUID().toString();
        String email = "DisposerMergerTest-" + userId + "@example.org";

        DeletionLog deletionLog = lauDeletionLogEntryProvider.createDeletionLogLau(userId, email);
        String activeUserId = idamUserDataProvider.createIdamUserWithEmail(email);
        String beginTime = "2024-01-14T14:15:23Z";

        // Assign role to archived user
        RoleAssignmentsAssignRoleRequest roleRequest1 = createRoleAssignmentRequest(
            deletionLog.getUserId(),
            "1547572255509701",
            CREATOR,
            beginTime
        );
        roleAssignmentProvider.assignRole(roleRequest1);

        // Assign role to active user
        RoleAssignmentsAssignRoleRequest roleRequest2 = createRoleAssignmentRequest(
            activeUserId,
            "1695398101351480",
            CLAIMANT,
            beginTime
        );
        roleAssignmentProvider.assignRole(roleRequest2);

        RoleAssignmentResponse initialArchivedUserRole =
            roleAssignmentProvider.getRoleAssignments(deletionLog.getUserId());
        assertThat(initialArchivedUserRole.getRoleAssignments()).hasSize(1);

        RoleAssignmentResponse newUserRole = roleAssignmentProvider.getRoleAssignments(activeUserId);
        assertThat(newUserRole.getRoleAssignments()).hasSize(1);

        // Run merger service
        idamDuplicateUserMergerService.run();

        // Check what has happened
        RoleAssignmentResponse activeUserRoleAssignmentAfterMerge =
            roleAssignmentProvider.getRoleAssignments(activeUserId);

        List<RoleAssignment> roleAssignments = activeUserRoleAssignmentAfterMerge.getRoleAssignments();
        assertThat(roleAssignments).hasSize(2);

        var roleAssignment1 = findRoleAssignment(roleAssignments, CREATOR);
        var roleAssignment2 = findRoleAssignment(roleAssignments, CLAIMANT);

        assertRole(activeUserId, roleAssignment1, roleRequest1);
        assertRole(activeUserId, roleAssignment2, roleRequest2);
    }

    @Test
    @DirtiesContext
    void havingTwoRolesShouldMergeBothOntoActiveUser() {
        String archivedUserId = UUID.randomUUID().toString();
        String email = "DisposerMergerTest-" + archivedUserId + "@example.org";
        DeletionLog deletionLog = lauDeletionLogEntryProvider.createDeletionLogLau(archivedUserId, email);

        String activeUserId = idamUserDataProvider.createIdamUserWithEmail(email);
        String beginTime = "2024-01-14T14:15:23Z";

        // Assign two roles to archived user
        String oldCaseId = "1547572255509701";
        var roleRequest1 = createRoleAssignmentRequest(archivedUserId, oldCaseId, CREATOR, beginTime);
        var roleRequest2 = createRoleAssignmentRequest(archivedUserId, oldCaseId, CLAIMANT, beginTime);
        roleAssignmentProvider.assignRole(roleRequest1);
        roleAssignmentProvider.assignRole(roleRequest2);

        // Assign role to active user
        String caseId = "1695398101351480";
        var roleRequest3 = createRoleAssignmentRequest(activeUserId, caseId, "[DEFENDANT]", beginTime);
        roleAssignmentProvider.assignRole(roleRequest3);


        RoleAssignmentResponse initialArchivedUserRole =
            roleAssignmentProvider.getRoleAssignments(deletionLog.getUserId());
        assertThat(initialArchivedUserRole.getRoleAssignments()).hasSize(2);

        RoleAssignmentResponse newUserRole = roleAssignmentProvider.getRoleAssignments(activeUserId);
        assertThat(newUserRole.getRoleAssignments()).hasSize(1);

        // Run merger service
        idamDuplicateUserMergerService.run();

        // Check what has happened
        RoleAssignmentResponse activeUserRoleAssignmentsAfterMergeResponse =
            roleAssignmentProvider.getRoleAssignments(activeUserId);
        List<RoleAssignment> roleAssignments = activeUserRoleAssignmentsAfterMergeResponse.getRoleAssignments();
        assertThat(roleAssignments).hasSize(3);

        var roleAssignment1 = findRoleAssignment(roleAssignments, CREATOR);
        var roleAssignment2 = findRoleAssignment(roleAssignments, CLAIMANT);
        var roleAssignment3 = findRoleAssignment(roleAssignments, "[DEFENDANT]");

        assertRole(activeUserId, roleAssignment1, roleRequest1);
        assertRole(activeUserId, roleAssignment2, roleRequest2);
        assertRole(activeUserId, roleAssignment3, roleRequest3);
    }

    private RoleAssignment findRoleAssignment(List<RoleAssignment> roleAssignments, String searchedRoleName) {
        return roleAssignments.stream()
            .filter(am -> searchedRoleName.equalsIgnoreCase(am.getRoleName()))
            .findFirst()
            .orElse(null);
    }

    private void assertRole(String userId, RoleAssignment response, RoleAssignmentsAssignRoleRequest request) {
        var requestRole = request.getRequestedRoles().get(0);
        assertThat(response.getActorId()).isEqualTo(userId);
        assertThat(response.getActorIdType()).isEqualTo("IDAM");
        assertThat(response.getRoleName()).isEqualTo(requestRole.getRoleName());
        assertThat(response.getRoleType()).isEqualTo(requestRole.getRoleType());
        assertThat(response.getRoleCategory()).isEqualTo(requestRole.getRoleCategory());
        assertThat(response.getBeginTime()).isEqualTo(requestRole.getBeginTime());
        assertThat(response.getClassification()).isEqualTo(requestRole.getClassification());
        assertThat(response.getGrantType()).isEqualTo(requestRole.getGrantType());
        assertThat(response.isReadOnly()).isEqualTo(requestRole.isReadOnly());
        assertThat(response.getAttributes().getCaseId()).isEqualTo(requestRole.getAttributes().getCaseId());
        assertThat(response.getAttributes().getCaseType()).isEqualTo(requestRole.getAttributes().getCaseType());
        assertThat(response.getAttributes().getJurisdiction()).isEqualTo(requestRole.getAttributes().getJurisdiction());
        assertThat(response.getAttributes().getSubstantive()).isEqualTo(requestRole.getAttributes().getSubstantive());
    }

    private RoleAssignmentsAssignRoleRequest createRoleAssignmentRequest(
        String userId, String caseId, String roleName, String beginTime
    ) {
        RoleRequest roleRequest = RoleRequest.builder()
            .requestType("CREATE")
            .process("CCD")
            .reference(caseId + "-" + userId)
            .assignerId(userId)
            .replaceExisting(false)
            .build();

        RoleAssignmentAttributes attributes = RoleAssignmentAttributes.builder()
            .substantive("N")
            .caseId(caseId)
            .jurisdiction("SSCS")
            .caseType("Benefit")
            .build();

        List<RequestedRole> requestedRoles = List.of(
            RequestedRole.builder()
                .actorIdType("IDAM")
                .actorId(userId)
                .roleType("CASE")
                .roleName(roleName)
                .classification("RESTRICTED")
                .grantType("SPECIFIC")
                .roleCategory("CITIZEN")
                .readOnly(false)
                .beginTime(beginTime)
                .process("CCD")
                .attributes(attributes)
                .build()
        );

        return RoleAssignmentsAssignRoleRequest.builder()
            .roleRequest(roleRequest)
            .requestedRoles(requestedRoles)
            .build();

    }
}
