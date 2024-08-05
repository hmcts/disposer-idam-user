package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RetrySteps extends WireMockStubs {

    @Autowired
    private IdamUserDisposerService service;

    @Given("IdAM api responds with 401")
    public void idamApiRespondsWith401() {
        wiremock.resetRequests();
        setupWireMock();
        setIdamApiStubToReturn401();
    }

    @Given("IdAM api responds with 500")
    public void idamApiRespondsWith500() {
        wiremock.resetRequests();
        setupWireMock();
        setIdamApiStubToReturn500();
    }

    @Then("it should retry making IdAM call")
    public void itShouldRetryMakingIdamCall() {
        service.run();
        wiremock.verify(2, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/v1/staleUsers")));

        wiremock.verify(WireMock.postRequestedFor(
            WireMock.urlPathEqualTo("/o/token")
        ));
        wiremock.verify(WireMock.postRequestedFor(WireMock.urlPathEqualTo("/lease")));
    }

    @Then("it should throw exception")
    public void itShouldthrowException() {
        try {
            service.run();
            fail("This line should not be reached");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }
}
