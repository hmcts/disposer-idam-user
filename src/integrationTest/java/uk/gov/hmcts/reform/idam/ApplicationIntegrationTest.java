package uk.gov.hmcts.reform.idam;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 4554)
class ApplicationIntegrationTest {

    @Autowired
    private IdamUserDisposerService service;

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
                .withHeader("Content-Type", "application/json")
                .withBodyFile("role-assignment-normal.json")

        ));

        var result = service.run();
        assertThat(result)
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

