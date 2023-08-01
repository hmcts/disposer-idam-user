package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserDisposerSteps extends WireMockStubs {

    @Autowired
    private IdamUserDisposerService service;

    @Value("${idam.client.delete_user_path}")
    private String deleteUserPath;

    private List<String> idamUserIds;

    @SuppressWarnings("PMD.JUnit4TestShouldUseBeforeAnnotation")
    @Before
    public void setUp() {
        setupWireMock();
    }

    @Given("User disposer runs")
    public void userDisposerRuns() {
        idamUserIds = service.run();
    }

    @Then("it should dispose users without roles")
    public void itShouldDisposeUsersWithoutRoles() {
        assertThat(idamUserIds).isNotEmpty();
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
        wiremock.verify(0, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6001")));
        wiremock.verify(0, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6002")));
        wiremock.verify(0, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6010")));
        wiremock.verify(0, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6023")));

        wiremock.verify(1, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6003")));
        wiremock.verify(1, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6011")));
        wiremock.verify(1, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6021")));
        wiremock.verify(1, deleteRequestedFor(urlPathEqualTo(deleteUserPath + "13e31622-edea-493c-8240-9b780c9d6024")));

        wiremock.verify(21, deleteRequestedFor(urlPathMatching(deleteUserPath + "([0-9a-zA-Z-]+)")));
    }

}
