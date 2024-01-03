package uk.gov.hmcts.reform.idam.service.remote.client;

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
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.service.remote.requests.RestoreUserRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.IdamQueryResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.List;
import java.util.Map;

@FeignClient(name = "idamClient", url = "${idam.api.url}")
@SuppressWarnings({"PMD.UseObjectForClearerAPI"})
public interface IdamClient {

    @GetMapping(value = Constants.STALE_USERS_PATH, consumes = "application/json", produces = "application/json")
    StaleUsersResponse getStaleUsers(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam Map<String, Object> queryParams
    );

    @PostMapping(
        value = Constants.STALE_USERS_PATH + "/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    Response restoreUser(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable(name = "userId") String userId,
        @RequestBody RestoreUserRequest restoreUserRequest
    );

    @GetMapping(
        value = Constants.IDAM_QUERY_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    List<IdamQueryResponse> queryUser(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam Map<String, String> queryParams
    );

    @DeleteMapping(
            value = Constants.STALE_USERS_PATH + "/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Response deleteUser(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable(name = "userId") String userId
    );

    @PostMapping(
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponse getToken(
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("grant_type") String grantType,
        @RequestParam("username") String username,
        @RequestParam("password") String password,
        @RequestParam("scope") String scope
    );

}
