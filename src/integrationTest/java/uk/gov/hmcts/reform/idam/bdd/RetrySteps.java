package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.client.WireMock;
import feign.FeignException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import static org.junit.jupiter.api.Assertions.assertThrows;

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

        // these are not called from mocked bean, but given that we have 2 calls in above, gives
        // some assurance that retry logic works
        // wiremock.verify(WireMock.postRequestedFor(
        //     WireMock.urlPathEqualTo("/o/token")
        // ));
        // wiremock.verify(WireMock.postRequestedFor(WireMock.urlPathEqualTo("/lease")));
    }

    @Then("it should rethrow exception")
    public void itShouldRethrowException() {
        assertThrows(FeignException.InternalServerError.class, () -> service.run());
    }
}
