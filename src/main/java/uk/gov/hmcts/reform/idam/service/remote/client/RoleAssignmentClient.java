package uk.gov.hmcts.reform.idam.service.remote.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsMergeRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsQueryRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.idam.util.Constants.ROLE_ASSIGNMENTS_ACTOR_PATH;
import static uk.gov.hmcts.reform.idam.util.Constants.ROLE_ASSIGNMENTS_PATH;
import static uk.gov.hmcts.reform.idam.util.Constants.ROLE_ASSIGNMENTS_QUERY_PATH;

@FeignClient(name = "roleAssignmentClient", url = "${ccd.role.assignment.host}")
public interface RoleAssignmentClient {

    @PostMapping(ROLE_ASSIGNMENTS_QUERY_PATH)
    RoleAssignmentResponse getRoleAssignments(
            @RequestHeader Map<String, String> headers,
            @RequestBody RoleAssignmentsQueryRequest body
    );

    @GetMapping(ROLE_ASSIGNMENTS_ACTOR_PATH)
    RoleAssignmentResponse getRoleAssignmentsByUserId(
        @RequestHeader Map<String, String> headers,
        @PathVariable("userId") String userId
    );

    @PostMapping(ROLE_ASSIGNMENTS_PATH)
    Response createRoleAssignment(
        @RequestHeader Map<String, String> headers,
        @RequestBody RoleAssignmentsMergeRequest body
    );

}
