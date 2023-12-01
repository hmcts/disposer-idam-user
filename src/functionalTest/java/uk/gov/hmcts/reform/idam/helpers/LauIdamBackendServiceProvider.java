package uk.gov.hmcts.reform.idam.helpers;

import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.ServiceTokenGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
public class LauIdamBackendServiceProvider {
    private final IdamTokenGenerator idamTokenGenerator;
    private final ServiceTokenGenerator serviceTokenGenerator;

    @Value("${lau.api.url}")
    private String lauIdamBackendUrl;

    public int deleteLogEntry(final String userId) {
        return RestAssured.given()
            .baseUri(lauIdamBackendUrl)
            .header("Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader())
            .header("ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken())
            .contentType("application/json")
            .param("userId", userId)
            .delete("/audit/idamUser/deleteIdamUserRecord")
            .getStatusCode();
    }

    public DeletedUsersResponse postLogEntry(final DeletedAccountsRequest deletedAccountsRequest) {
        return RestAssured.given()
            .baseUri(lauIdamBackendUrl)
            .header("Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader())
            .header("ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken())
            .contentType("application/json")
            .accept("application/json")
            .body(deletedAccountsRequest)
            .when()
            .post("/audit/deletedAccounts")
            .then()
            .extract().body().as(DeletedUsersResponse.class);
    }
}
