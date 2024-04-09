package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.aop.Retry;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsQueryRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.idam.util.Constants.ROLE_ASSIGNMENTS_CONTENT_TYPE;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final RoleAssignmentClient roleAssignmentClient;
    private final SecurityUtil securityUtil;
    private final ParameterResolver parameterResolver;

    @Retry(retryAttempts = 2)
    public List<String> filterUsersWithRoles(List<String> staleUsers) {
        if (staleUsers.isEmpty()) {
            return List.of();
        }

        RoleAssignmentsQueryRequest request = new RoleAssignmentsQueryRequest(staleUsers);
        final RoleAssignmentResponse response;

        try {
            response = getRoleAssignmentResponse(request);
        } catch (Exception e) {
            log.error("UserRoleService.getRoleAssignemnts threw exception: {}", e.getMessage(), e);
            throw e;
        }

        List<String> assignments = response
            .getRoleAssignments()
            .stream()
            .filter(assignment -> "IDAM".equals(assignment.getActorIdType()))
            .map(RoleAssignment::getActorId)
            .toList();
        return staleUsers.stream()
            .filter(userId -> !assignments.contains(userId))
            .toList();
    }

    private RoleAssignmentResponse getRoleAssignmentResponse(final RoleAssignmentsQueryRequest request) {
        int page = 0;
        final int querySize = Math.min(
            parameterResolver.getBatchSize() * 10, 1000); // the query size can't be greater then 1000

        final RoleAssignmentResponse response = roleAssignmentClient.getRoleAssignments(
            getHeaders(page, querySize),
            request
        );

        int roleAssignmentsQueryResponseSize = response.getRoleAssignments().size();

        while (roleAssignmentsQueryResponseSize == querySize) {
            final List<RoleAssignment> roleAssignments = roleAssignmentClient.getRoleAssignments(getHeaders(
                ++page,
                querySize
            ), request).getRoleAssignments();

            roleAssignmentsQueryResponseSize = roleAssignments.size();

            response.getRoleAssignments().addAll(roleAssignments);
        }
        return response;
    }


    private Map<String, String> getHeaders(final int pageNumber, final int size) {
        Map<String, String> headers = new ConcurrentHashMap<>(securityUtil.getAuthHeaders());
        headers.put("Content-Type", ROLE_ASSIGNMENTS_CONTENT_TYPE);
        headers.put("pageNumber", String.valueOf(pageNumber));
        headers.put("size", String.valueOf(size));
        return headers;
    }
}
