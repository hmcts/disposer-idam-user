package uk.gov.hmcts.reform.idam.service.remote;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequests;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;

import java.util.Map;

@FeignClient(name = "idamClient", url = "${idam.api.url}")
public interface IdamClient {

    @GetMapping(value = "${idam.client.stale_users_path}", consumes = "application/json", produces = "application/json")
    StaleUsersResponse getStaleUsers(@RequestParam Map<String, Object> queryParams);

    @PostMapping("${idam.client.role_assignments_path}")
    RoleAssignmentResponse getRoleAssignments(
        @RequestHeader Map<String, String> headers,
        @RequestBody UserRoleAssignmentQueryRequests body
        );

    @DeleteMapping(
        value = "${idam.client.delete_user_path}{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    Response deleteUser(@PathVariable(name = "userId") String userId);
}
