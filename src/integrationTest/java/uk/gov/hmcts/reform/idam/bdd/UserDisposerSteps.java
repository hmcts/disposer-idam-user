package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserDisposerSteps extends AbstractSteps {

    @Autowired
    private IdamUserDisposerService service;

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

    @Then("it should collect users to dispose")
    public void itShouldCollectUsersToDispose() {
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
    }

}
