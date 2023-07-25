package uk.gov.hmcts.reform.idam.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.RestClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RequestBody;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequests;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final ParameterResolver idamConfig;
    private final RestClient client;

    public List<String> filterUsersWithRoles(List<String> staleUsers) {
        Map<String, String> headers = Map.of("Content-Type", idamConfig.getRoleAssignmentsContentType());
        var roleAssignmentQuery = UserRoleAssignmentQueryRequest.builder().userIds(staleUsers).build();
        RequestBody body = UserRoleAssignmentQueryRequests.builder().queryRequests(roleAssignmentQuery).build();

        final Response response = client.postRequest(
            idamConfig.getIdamHost(),
            idamConfig.getRoleAssignmentsPath(),
            headers,
            body);

        if (response.getStatus() == OK.value()) {
            var assignmentsResponse = response.readEntity(RoleAssignmentResponse.class);
            var assignments = assignmentsResponse
                .getRoleAssignments()
                .stream()
                .filter(assignment -> "IDAM".equals(assignment.getActorIdType()))
                .map(RoleAssignment::getActorId)
                .toList();
            return staleUsers.stream()
                .filter(userId -> !assignments.contains(userId))
                .toList();
        } else {
            log.error(String.format(
                "User role assignments IDAM API call failed, status '%s', body: '%s'",
                response.getStatus(),
                response.readEntity(String.class)));
            throw new IdamApiException(
                String.format(
                    "User role assignments IDAM API call failed, status '%s', body: '%s'",
                    response.getStatus(),
                    response.readEntity(String.class))
            );
        }
    }
}
