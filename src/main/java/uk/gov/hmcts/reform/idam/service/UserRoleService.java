package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.aop.Retry;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsPostRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.ServiceTokenGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.idam.util.Constants.ROLE_ASSIGNMENTS_CONTENT_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final RoleAssignmentClient roleAssignmentClient;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ServiceTokenGenerator serviceTokenGenerator;

    @Retry(retryAttempts = 2)
    public List<String> filterUsersWithRoles(List<String> staleUsers) {
        if (staleUsers.isEmpty()) {
            return List.of();
        }

        var request = new RoleAssignmentsPostRequest(staleUsers);
        final RoleAssignmentResponse response;

        try {
            response = roleAssignmentClient.getRoleAssignments(
                Map.of(
                    "Content-Type", ROLE_ASSIGNMENTS_CONTENT_TYPE,
                    "Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader(),
                    "ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken()
                ),
                request
            );
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
}
