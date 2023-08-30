package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequests;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final RoleAssignmentClient roleAssignmentClient;

    public List<String> filterUsersWithRoles(List<String> staleUsers) {
        var roleAssignmentQuery = UserRoleAssignmentQueryRequest.builder().userIds(staleUsers).build();
        UserRoleAssignmentQueryRequests body = UserRoleAssignmentQueryRequests.builder()
            .queryRequests(roleAssignmentQuery)
            .build();

        final RoleAssignmentResponse response = roleAssignmentClient.getRoleAssignments(
            Map.of("Content-Type", Constants.ROLE_ASSIGNMENTS_CONTENT_TYPE),
            body
        );

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
}
