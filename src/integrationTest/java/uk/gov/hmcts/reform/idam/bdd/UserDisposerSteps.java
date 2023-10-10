package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.Boolean.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.idam.util.Constants.STALE_USERS_PATH;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserDisposerSteps extends WireMockStubs {

    @Autowired
    private IdamUserDisposerService service;

    @Autowired
    private ParameterResolver parameterResolver;

    @Autowired
    private SecurityUtil securityUtil;

    @Given("IdAM api works fine and simulation mode is {string}")
    public void idamApiWorksFine(final String simulationMode) {
        wiremock.resetRequests();
        setupWireMock();
        setupIdamApiStubsForSuccess();
        setField(parameterResolver, "isSimulation", valueOf(simulationMode));
    }

    @Then("it should dispose users without roles")
    public void itShouldDisposeUsersWithoutRoles() {
        List<String> idamUserIds = service.run();

        verifyIdamGetStaleUsersAndRoleAssignmentsApisInvoked(idamUserIds);
        verifyDeleteUsersApiInvoked();
    }

    @Then("it should not dispose users due to simulation mode")
    public void itShouldNotDisposeUsersDueToSimulationMode() {
        List<String> idamUserIds = service.run();

        verifyIdamGetStaleUsersAndRoleAssignmentsApisInvoked(idamUserIds);
        wiremock.verify(0, deleteRequestedFor(urlPathMatching(STALE_USERS_PATH + "/([0-9a-zA-Z-]+)")));
    }

    private void verifyIdamGetStaleUsersAndRoleAssignmentsApisInvoked(final List<String> idamUserIds) {
        assertThat(idamUserIds).isNotEmpty();

        wiremock.verify(getRequestedFor(urlPathEqualTo("/api/v1/staleUsers"))
                .withHeader("Authorization", equalTo("Bearer " + dummyJwtToken)));

        wiremock.verify(postRequestedFor(urlPathEqualTo("/am/role-assignments/query"))
                .withHeader("Authorization", equalTo("Bearer " + dummyJwtToken))
                .withHeader("ServiceAuthorization", equalTo("Bearer " + dummyJwtToken)));

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
    }

    private void verifyDeleteUsersApiInvoked() {
        String deleteUserPath = STALE_USERS_PATH;
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
        wiremock.verify(1, deleteRequestedFor(
                urlPathEqualTo(deleteUserPath + "/13e31622-edea-493c-8240-9b780c9d6025")));

        wiremock.verify(14, deleteRequestedFor(urlPathMatching(deleteUserPath + "/([0-9a-zA-Z-]+)")));
    }

}
