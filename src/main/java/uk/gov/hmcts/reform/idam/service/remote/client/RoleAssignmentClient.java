package uk.gov.hmcts.reform.idam.service.remote.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequests;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.idam.util.Constants.ROLE_ASSIGNMENTS_PATH;

@FeignClient(name = "roleAssignmentClient", url = "${ccd.role.assignment.host}")
public interface RoleAssignmentClient {

    @PostMapping(ROLE_ASSIGNMENTS_PATH)
    RoleAssignmentResponse getRoleAssignments(
            @RequestHeader Map<String, String> headers,
            @RequestBody UserRoleAssignmentQueryRequests body
    );
}
