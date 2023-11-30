package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.IdamUserRestorerService;
import uk.gov.hmcts.reform.idam.service.LauIdamUserService;
import uk.gov.hmcts.reform.idam.util.Constants;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRestorerSteps extends WireMockStubs {

    @Autowired
    private IdamUserRestorerService service;

    @Autowired
    private LauIdamUserService lauIdamUserService;

    @Autowired
    private RestoreSummary restoreSummary;

    @Given("lau api returns a list of users to restore")
    public void lauIdamReturnsListOfUsersToRestore() {
        wiremock.resetAll();
        setupWireMock();
        setupLauApiStubs();
        setupIdamRestoreStub(201, null);
    }

    @Given("lau api returns paged results")
    public void lauApiReturnsPagedResults() {
        wiremock.resetAll();
        setupWireMock();
        setupPagedLauApiStubs();
        setupIdamRestoreStub(201, null);
    }

    @When("restore service runs")
    public void restoreServiceRuns() {
        service.run();
    }

    @Then("there should be {int} calls to fetch deleted users")
    public void thereShouldBeCallsToFetchDeletedUsers(int numberOfCalls) {
        wiremock.verify(numberOfCalls, getRequestedFor(urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH)));
    }

    @Then("user restorer should call IdAM api to restore {int} users")
    public void userRestorerShouldCallIdamApiToRestoreUsers(int numberToRestore) {
        wiremock.verify(numberToRestore, postRequestedFor(urlPathMatching(Constants.STALE_USERS_PATH + "/0000[1-4]")));
    }

    @Given("requests limit set to {int}")
    public void requestsLimitSetTo(int requestLimit) {
        wiremock.resetAll();
        setupWireMock();
        setupLauIdamApiStubToReturnMoreRecords();
        setupIdamRestoreStub(201, null);

        ReflectionTestUtils.setField(service, "requestsLimit", requestLimit);
        ReflectionTestUtils.setField(lauIdamUserService, "batchSize", 2);
    }

    @Then("there should be {int} requests to lau api")
    public void thereShouldBeRequestsToLauIdAM(int requestNumber) {
        wiremock.verify(requestNumber, getRequestedFor(urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH)));
        wiremock.verify(requestNumber, postRequestedFor(urlPathEqualTo(Constants.STALE_USERS_PATH + "/00001")));
    }

    @Then("summary should have successful restore of size {int} and failed of size {int}")
    public void summaryShouldHaveSuccessfulRestoreOfSize(int successes, int failures) {
        assertThat(restoreSummary.getSuccessful()).hasSize(successes);
        assertThat(restoreSummary.getFailed()).hasSize(failures);
    }

    @Given("IdAM api responds with {int} error to user restore call")
    public void idamApiRespondsWithErrorToEndpointPostCall(int errorCode) {
        wiremock.resetRequests();
        setupWireMock();
        setupIdamRestoreStub(errorCode, "Bad Request");
    }
}
