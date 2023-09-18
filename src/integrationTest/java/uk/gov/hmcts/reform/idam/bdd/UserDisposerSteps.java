package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserDisposerSteps extends WireMockStubs {

    @Autowired
    private IdamUserDisposerService service;

    @Given("IdAM api works fine")
    public void idamApiWorksFine() {
        wiremock.resetScenarios();
        setupWireMock();
        setupIdamApiStubsForSuccess();
    }

    @Then("it should dispose users without roles")
    public void itShouldDisposeUsersWithoutRoles() {
        List<String> idamUserIds = service.run();
        assertThat(idamUserIds).isNotEmpty();

        wiremock.verify(getRequestedFor(urlPathEqualTo("/api/v1/staleUsers"))
                            .withHeader("Authorization", equalTo("Bearer token")));

        wiremock.verify(postRequestedFor(urlPathEqualTo("/am/role-assignments/query"))
                            .withHeader("Authorization", equalTo("Bearer token"))
                            .withHeader("ServiceAuthorization", equalTo("dummy token")));

        assertThat(idamUserIds).doesNotContain(
            "13e31622-edea-493c-8240-9b780c9d6001",
            "13e31622-edea-493c-8240-9b780c9d6002",
            "13e31622-edea-493c-8240-9b780c9d6010",
            "13e31622-edea-493c-8240-9b780c9d6023"
        );
        assertThat(idamUserIds).contains(
            "13e31622-edea-493c-8240-9b780c9d6003",
            "13e31622-edea-493c-8240-9b780c9d6015",
            "13e31622-edea-493c-8240-9b780c9d6020",
            "13e31622-edea-493c-8240-9b780c9d6025"
        );
        String deleteUserPath = Constants.STALE_USERS_PATH;
        wiremock.verify(0, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6001")));
        wiremock.verify(0, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6002")));
        wiremock.verify(0, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6010")));
        wiremock.verify(0, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6023")));

        wiremock.verify(1, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6003")));
        wiremock.verify(1, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6011")));
        wiremock.verify(1, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6021")));
        wiremock.verify(1, deleteRequestedFor(
            urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6024")));

        wiremock.verify(21, deleteRequestedFor(urlPathMatching(deleteUserPath + "/([0-9a-zA-Z-]+)")));
    }

}
