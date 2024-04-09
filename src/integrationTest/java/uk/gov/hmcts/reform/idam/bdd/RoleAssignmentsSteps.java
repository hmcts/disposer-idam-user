package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.idam.service.UserRoleService;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class RoleAssignmentsSteps extends RoleAssignmentStubs {

    private final UserRoleService userRoleService;

    private List<String> collected;

    @Value("${role-assignments.max-page-size}")
    private int maxPageSize;

    @Given("Role Assignments filtering called with many users")
    public void callRoleAssignments() {
        wiremock.resetRequests();
        setupRoleAssignmentPagedStub();

        collected = userRoleService.filterUsersWithRoles(List.of(
            "13e31622-edea-493c-8240-9b780c9d6001",
            "8fa77679-871a-4e63-9968-d3dca91ef86e"
        ));
    }

    @Then("Role Assignments should fetch all available pages")
    public void checkResults() {
        assertThat(collected).hasSize(1);
        assertThat(collected).doesNotContain("8fa77679-871a-4e63-9968-d3dca91ef86e");
        for (int i = 0; i < 3; i++) {
            wiremock.verify(1,
                WireMock.postRequestedFor(WireMock.urlPathEqualTo(Constants.ROLE_ASSIGNMENTS_QUERY_PATH))
                    .withHeader("pageNumber", WireMock.equalTo(String.valueOf(i)))
                    .withHeader("size", WireMock.equalTo(String.valueOf(maxPageSize)))
            );
        }
        wiremock.verify(3, WireMock.postRequestedFor(WireMock.urlPathEqualTo(Constants.ROLE_ASSIGNMENTS_QUERY_PATH)));
    }
}
