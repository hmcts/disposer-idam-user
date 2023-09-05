package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
public class WireMockStubs {
    public final WireMockServer wiremock = WireMockInstantiator.INSTANCE.getWireMockServer();

    public void setupWireMock() {

        wiremock.addMockServiceRequestListener(
            WireMockStubs::requestReceived);
        setupAuthorizationStub();
        setupIdamApiStubs();
    }

    private void setupIdamApiStubs() {
        for (int i = 0; i < 3; i++) {
            // add 25 user ids with uuid
            // "13e31622-edea-493c-8240-9b780c9d6001"
            // changing only the last three bits (001 to 025)
            wiremock.stubFor(
                WireMock
                    .get(WireMock.urlPathEqualTo(Constants.STALE_USERS_PATH))
                    .withQueryParam("page", equalTo(String.valueOf(i)))
                    .willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("staleUsersPage" + (i + 1) + ".json")
                    )
            );

            // pretend that 001, 002, 010 and 023 still have assigned roles
            wiremock.stubFor(
                WireMock
                    .post(WireMock.urlPathEqualTo(Constants.ROLE_ASSIGNMENTS_PATH))
                    .withRequestBody(WireMock.matchingJsonPath(
                                         "$.queryRequests.actorId",
                                         WireMock.containing(getMatchingActorId(i + 1))
                                     )
                    )
                    .willReturn(
                        WireMock.aResponse()
                            .withBodyFile("roleAssignmentsResponse" + (i + 1) + ".json")
                            .withHeader("Content-Type", Constants.ROLE_ASSIGNMENTS_CONTENT_TYPE)
                    )
            );
        }

        // delete endpoint
        wiremock.stubFor(
            WireMock
                .delete(WireMock.urlPathMatching(Constants.STALE_USERS_DELETE_PATH + "/([0-9a-zA-Z-]+)"))
                .willReturn(WireMock.aResponse().withStatus(NO_CONTENT.value()))
        );
    }

    protected static void requestReceived(Request request, Response response) {
        log.trace("WireMock request at URL: {}", request.getAbsoluteUrl());
        log.trace("WireMock request headers: \n{}", request.getHeaders());
        log.trace("WireMock response body: \n{}", response.getBodyAsString());
        log.trace("WireMock response headers: \n{}", response.getHeaders());
    }

    private String getMatchingActorId(int page) {
        // UUID is from wiremock/__files/staleUsersPage{1,2,3}.json
        return "13e31622-edea-493c-8240-9b780c9d60" + (page - 1) + "1";
    }


    private void setupAuthorizationStub() {
        wiremock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/details"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                        .withStatus(200)
                        .withBody("disposer")
                )
        );

        wiremock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/o/userinfo"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                        .withStatus(200)
                        .withBody(getUserInfoAsJson())
                )
        );
    }

    private String getUserInfoAsJson() {
        return new Gson().toJson(
            new UserInfo(
                "sub",
                "uid",
                "test",
                "given_name",
                "family_Name",
                List.of("cft-audit-investigator")
            )
        );
    }


}
