package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Getter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

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
    }

}
