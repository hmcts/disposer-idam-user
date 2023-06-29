package uk.gov.hmcts.reform.idam;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.service.StaleUsersService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.idam.service.StaleUsersService.ROLE_ASSIGNMENTS_CONTENT_TYPE;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureWireMock(port = 4554)
class ApplicationIntegrationTest {

    @Autowired
    private StaleUsersService service;

    private static final String FLAG = "true";

    @Test
    void testShouldBootstrapSpringContext() {
        WireMock.listAllStubMappings();
        assertThat(Boolean.valueOf(FLAG)).isTrue();
    }

    @Test
    void testShouldReturnUsersWithoutRoles() {
        stubFor(get(urlPathEqualTo("/api/v2/staleUsers")).willReturn(
            aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("stale-users-normal.json")
        ));

        stubFor(post(urlPathEqualTo("/am/role-assignments/query")).willReturn(
            aResponse()
                .withBodyFile("role-assignment-normal.json")
                .withHeader("Content-Type", ROLE_ASSIGNMENTS_CONTENT_TYPE)
        ));

        var staleUsers = service.retrieveStaleUsers();
        var response = service.filterUsersWithRoleAssignments(staleUsers);
        assertThat(response)
            .contains("003", "004", "005", "006", "007", "008", "009")
            .doesNotContain("001", "002", "010");
    }


    @TestConfiguration
    static class WireMockTestConfiguration {
        @Bean
        WireMockConfigurationCustomizer optionsCustomizer() {

            return config -> {
                config.notifier(new ConsoleNotifier(true));
                config.usingFilesUnderDirectory("src/integrationTest/resources/wiremock");
            };

        }
    }

}

