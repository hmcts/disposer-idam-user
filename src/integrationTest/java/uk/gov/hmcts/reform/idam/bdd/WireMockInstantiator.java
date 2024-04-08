package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Getter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Getter
public enum WireMockInstantiator {
    INSTANCE;

    private static final int IDAM_API_PORT = 5000;

    private final WireMockServer wireMockServer;

    WireMockInstantiator() {
        wireMockServer = new WireMockServer(
            options()
                .port(IDAM_API_PORT)
                .usingFilesUnderClasspath("wiremock")
        );
        wireMockServer.start();
    }

    public static WireMockServer getWireMockInstance() {
        return INSTANCE.getWireMockServer();
    }

}
