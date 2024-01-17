package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RequestedRole;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsMergeRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.DuplicateUserSummary;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.idam.service.IdamDuplicateUserMergerService.MARKER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleMergeService {

    private final RoleAssignmentClient roleAssignmentClient;
    private final SecurityUtil securityUtil;
    private final DuplicateUserSummary duplicateUserSummary;

    @Value("${duplicate-user-merger.dry_run}")
    private boolean dryRun;

    public void mergeRoleAssignments(String archivedUserId, String activeUserId) {
        var roleAssignmentResponse = roleAssignmentClient.getRoleAssignmentsByUserId(
            securityUtil.getAuthHeaders(),
            archivedUserId
        );

        List<RoleAssignmentsMergeRequest> mergeRequests = createMergeRequests(
            roleAssignmentResponse,
            archivedUserId,
            activeUserId
        );

        if (dryRun) {
            log.warn(
                "[{}] DRY RUN mode, not merging archived user id {}, active user id {}",
                MARKER, archivedUserId, activeUserId
            );
        } else {
            commitMergeRequests(mergeRequests, archivedUserId, activeUserId);
        }
    }

    private void commitMergeRequests(
        List<RoleAssignmentsMergeRequest> mergeRequests,
        String archivedUserId,
        String activeUserId
    ) {
        Map<String, String> headers = securityUtil.getAuthHeaders();
        for (RoleAssignmentsMergeRequest mergeRequest: mergeRequests) {
            List<String> caseTypes = mergeRequest.getRequestedRoles().stream()
                .map(roleAssignment -> roleAssignment.getAttributes().getCaseType())
                .toList();
            try (var mergeResponse = roleAssignmentClient.createRoleAssignment(headers, mergeRequest)) {
                if (mergeResponse.status() == 201) {
                    duplicateUserSummary.increaseMerged();
                    log.info(
                        "[{}] Merged archived user {} to active user {}, case types: {}",
                        MARKER, archivedUserId, activeUserId, caseTypes
                    );
                } else {
                    duplicateUserSummary.increaseFailedMerge();
                    log.error(
                        "[{}] Merge failed for user active user {} (deleted user id {})",
                        MARKER,
                        activeUserId,
                        archivedUserId
                    );
                }
            }
        }
    }

    private List<RoleAssignmentsMergeRequest> createMergeRequests(
        RoleAssignmentResponse response,
        String archivedUserId,
        String activeUserId
    ) {
        final List<RoleAssignmentsMergeRequest> mergeRequests = new LinkedList<>();
        List<RoleAssignment> roleAssignments = response.getRoleAssignments();
        if (roleAssignments == null || roleAssignments.isEmpty()) {
            duplicateUserSummary.increaseNoRoleAssignmentsOnUser();
            log.info("[{}] No roles assigned to archived user: {}", MARKER, archivedUserId);
            return mergeRequests;
        }
        Map<String, List<RoleAssignment>> caseIdRoleMapping = groupRoleAssignmentsByCaseId(roleAssignments);

        for (var entry: caseIdRoleMapping.entrySet()) {
            String reference = String.format("%s-%s", entry.getKey(), activeUserId);
            RoleRequest roleRequest = RoleRequest.builder()
                .requestType("CREATE")
                .process("CCD")
                .reference(reference)
                .assignerId(activeUserId) // ???
                .replaceExisting(false)
                .build();

            mergeRequests.add(
                RoleAssignmentsMergeRequest.builder()
                    .roleRequest(roleRequest)
                    .requestedRoles(createRoleRequests(entry.getValue(), activeUserId))
                    .build()
            );
        }

        return mergeRequests;
    }

    private List<RequestedRole> createRoleRequests(List<RoleAssignment> roleAssignments, String activeUserId) {
        return roleAssignments
            .stream()
            .map(roleAssignment -> buildRequestedRole(roleAssignment, activeUserId))
            .toList();
    }

    private RequestedRole buildRequestedRole(RoleAssignment roleAssignment, String activeUserId) {
        return RequestedRole.builder()
            .actorIdType(roleAssignment.getActorIdType())
            .actorId(activeUserId)
            .roleType(roleAssignment.getRoleType())
            .roleName(roleAssignment.getRoleName())
            .classification(roleAssignment.getClassification())
            .grantType(roleAssignment.getGrantType())
            .roleCategory(roleAssignment.getRoleCategory())
            .readOnly(roleAssignment.isReadOnly())
            .beginTime(roleAssignment.getBeginTime())
            .process("CCD")
            .attributes(roleAssignment.getAttributes())
            .build();
    }

    private Map<String, List<RoleAssignment>> groupRoleAssignmentsByCaseId(List<RoleAssignment> roleAssignments) {
        return roleAssignments.stream().collect(Collectors.groupingBy(role -> role.getAttributes().getCaseId()));
    }
}
