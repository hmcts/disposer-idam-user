package uk.gov.hmcts.reform.idam.bdd;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.IdamUserRestorerService;
import uk.gov.hmcts.reform.idam.util.Constants;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public class UserRestorerSteps extends WireMockStubs {

    @Autowired
    private IdamUserRestorerService service;

    @Given("lau api returns a list of users to restore")
    public void lauIdamReturnsListOfUsersToRestore() {
        setupWireMock(true);
        setupLauApiStubs();
    }

    @Then("user restorer should call IdAM api to restore users")
    public void userRestorerShouldCallIdamApiToRestoreUsers() {
        setupIdamRestoreStub(201, null);
        service.run();
        wiremock.verify(1, getRequestedFor(urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH)));
        wiremock.verify(4, postRequestedFor(urlPathMatching(Constants.STALE_USERS_PATH + "/0000[1-4]")));
    }

    @Then("user restorer should call lau api to delete log entries of deletion")
    public void userRestorerShouldCallLauIdamApiToDeleteLogEntriesOfDeletion() {
        wiremock.verify(
            4,
            deleteRequestedFor(
                urlMatching(Constants.LAU_DELETE_LOG_ENTRY_PATH + "\\?userId=0000[1-4]")
            )
        );
    }

    @And("IdAM api fails to restore with status code {int} and message {string}")
    public void idamApiFailsToRestoreWithResponse(int statusCode, String message) {
        wiremock.resetAll();
        setupWireMock();
        setupLauApiStubs();
        setupIdamRestoreStub(statusCode, message.isEmpty() ? null : message);
        service.run();
    }

    @Then("user restorer should NOT call lau api to delete log entries")
    public void userRestorerShouldNotCallLauIdamApiToDeleteLogEntries() {
        wiremock.verify(0, deleteRequestedFor(urlPathEqualTo(Constants.LAU_DELETE_LOG_ENTRY_PATH)));
    }

    @Given("requests limit set to {int}")
    public void requestsLimitSetTo(int requestLimit) {
        wiremock.resetAll();
        setupWireMock();
        setupLauIdamApiStubToReturnMoreRecords();
        setupIdamRestoreStub(201, null);

        ReflectionTestUtils.setField(service, "requestsLimit", requestLimit);
        ReflectionTestUtils.setField(service, "batchSize", 2);
        service.run();
    }

    @Then("there should be {int} requests to lau api")
    public void thereShouldBeRequestsToLauIdAM(int requestNumber) {
        wiremock.verify(10, getRequestedFor(urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH)));
        wiremock.verify(10, postRequestedFor(urlPathEqualTo(Constants.STALE_USERS_PATH + "/00001")));
    }
}
