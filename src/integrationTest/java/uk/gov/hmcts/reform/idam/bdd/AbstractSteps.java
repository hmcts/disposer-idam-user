package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class AbstractSteps {
    public final WireMockServer wiremock = WireMockInstantiator.INSTANCE.getWireMockServer();

    @Value("${idam.client.stale_users_path}")
    private String staleUsersPath;

    @Value("${idam.client.role_assignments_path}")
    private String roleAssignmentsPath;

    @Value("${idam.client.role_assignments_content_type}")
    private String roleAssignmentsContentType;

    public void setupWireMock() {
        setupAuthorizationStub();
        setupIdamApiStubs();
    }

    private void setupIdamApiStubs() {
        for (int i = 1; i < 4; i++) {
            // add 25 user ids with uuid
            // "13e31622-edea-493c-8240-9b780c9d6001"
            // changing only the last three bits (001 to 025)
            wiremock.stubFor(
                WireMock
                    .get(WireMock.urlMatching(staleUsersPath + "\\?pageNumber=" + i))
                    .willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("staleUsersPage" + i + ".json")
                    )
            );

            // pretend that 001, 002, 010 and 023 still have assigned roles

            wiremock.stubFor(
                WireMock
                    .post(WireMock.urlPathEqualTo(roleAssignmentsPath))
                    .withRequestBody(WireMock.matchingJsonPath(
                                         "$.queryRequests.actorId",
                                         WireMock.containing(getMatchingActorId(i))
                                     )
                    )
                    .willReturn(
                        WireMock.aResponse()
                            .withBodyFile("roleAssignmentsResponse" + i + ".json")
                            .withHeader("Content-Type", roleAssignmentsContentType)
                    )
            );
        }
    }

    private String getMatchingActorId(int pageNumber) {
        // UUID is from wiremock/__files/staleUsersPage{1,2,3}.json
        return "13e31622-edea-493c-8240-9b780c9d60" + (pageNumber - 1) + "1";
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
