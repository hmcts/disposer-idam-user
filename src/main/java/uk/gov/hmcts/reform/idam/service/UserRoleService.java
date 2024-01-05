package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.aop.Retry;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RequestedRole;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsMergeRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsQueryRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.DuplicateUserSummary;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final RoleAssignmentClient roleAssignmentClient;
    private final SecurityUtil securityUtil;
    private final DuplicateUserSummary duplicateUserSummary;

    @Retry(retryAttempts = 2)
    public List<String> filterUsersWithRoles(List<String> staleUsers) {
        if (staleUsers.isEmpty()) {
            return List.of();
        }

        var request = new RoleAssignmentsQueryRequest(staleUsers);
        final RoleAssignmentResponse response;

        try {
            response = roleAssignmentClient.getRoleAssignments(getHeaders(), request);
        } catch (Exception e) {
            log.error("UserRoleService.getRoleAssignemnts threw exception: {}", e.getMessage(), e);
            throw e;
        }

        var assignments = response
            .getRoleAssignments()
            .stream()
            .filter(assignment -> "IDAM".equals(assignment.getActorIdType()))
            .map(RoleAssignment::getActorId)
            .toList();
        return staleUsers.stream()
            .filter(userId -> !assignments.contains(userId))
            .toList();

    }

    public void mergeRoleAssignments(String archivedUserId, String activeUserId) {
        var response = roleAssignmentClient.getRoleAssignmentsByUserId(securityUtil.getAuthHeaders(), archivedUserId);

        RoleAssignmentsMergeRequest mergeRequest = createMergeRequest(response, archivedUserId, activeUserId);
        Map<String, String> headers = securityUtil.getAuthHeaders();
        try (var mergeResponse = roleAssignmentClient.createRoleAssignment(headers, mergeRequest)) {
            if (mergeResponse.status() >= 300) {
                duplicateUserSummary.increaseFailedMerge();
                log.error("Merge failed for user active user {} (deleted user id {})", activeUserId, archivedUserId);
            }
        }
    }

    private RoleAssignmentsMergeRequest createMergeRequest(
        RoleAssignmentResponse response,
        String archivedUserId,
        String activeUserId
    ) {
        List<RoleAssignment> roleAssignments = response.getRoleAssignments();
        String caseId = checkRoleAssignments(roleAssignments, archivedUserId);
        String reference = String.format("%s-%s", caseId, activeUserId);

        RoleRequest roleRequest = RoleRequest.builder()
            .requestType("CREATE")
            .process("CCD")
            .reference(reference)
            .assignerId(activeUserId) // ???
            .replaceExisting(false)
            .build();

        List<RequestedRole> requestedRoles = new LinkedList<>();
        for (var roleAssignment: roleAssignments) {
            requestedRoles.add(buildRequestedRole(roleAssignment, activeUserId));
        }

        return RoleAssignmentsMergeRequest.builder()
            .roleRequest(roleRequest)
            .requestedRoles(requestedRoles)
            .build();
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

    private String checkRoleAssignments(List<RoleAssignment> roleAssignments, String archivedUserId) {
        var caseIds = roleAssignments.stream()
            .map(RoleAssignment::getAttributes)
            .map(RoleAssignmentAttributes::getCaseId)
            .collect(Collectors.toSet());

        if (caseIds.size() > 1) {
            throw new UnsupportedOperationException("Different case ids found on a single user " + archivedUserId);
        }

        if (caseIds.isEmpty()) {
            throw new UnsupportedOperationException("User has no role assignments " + archivedUserId);
        }
        var iterator = caseIds.iterator();
        return iterator.next();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>(securityUtil.getAuthHeaders());
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
