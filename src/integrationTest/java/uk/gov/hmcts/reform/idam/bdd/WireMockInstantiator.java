package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Getter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
public enum WireMockInstantiator {
    INSTANCE;

    private int idamApiPort = 5000;

    private final WireMockServer wireMockServer;

    WireMockInstantiator() {
        wireMockServer = new WireMockServer(
                options()
                        .port(idamApiPort)
                        .usingFilesUnderClasspath("wiremock")
        );
        wireMockServer.start();

        with().await()
                .untilAsserted(() -> assertTrue(wireMockServer.isRunning(), "Verify wiremock is running"));
    }

}
