package uk.gov.hmcts.reform.idam.service.remote;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.Map;

@FeignClient(name = "idamClient", url = "${idam.api.url}")
public interface IdamClient {

    @GetMapping(value = Constants.STALE_USERS_PATH, consumes = "application/json", produces = "application/json")
    StaleUsersResponse getStaleUsers(@RequestParam Map<String, Object> queryParams);

    @DeleteMapping(
        value = Constants.DELETE_USER_PATH + "/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    Response deleteUser(@PathVariable(name = "userId") String userId);
}
