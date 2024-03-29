package uk.gov.hmcts.reform.idam.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
public class WireMockStubs {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APP_JSON = "application/json";

    @Value("${dummy-jwt}")
    String dummyJwtToken;

    @Value("${wiremock-debug:false}")
    private boolean debugging;

    public final WireMockServer wiremock = WireMockInstantiator.INSTANCE.getWireMockServer();

    public void setupWireMock() {
        setupWireMock(false);
    }

    public void setupWireMock(boolean debug) {
        if (debug) {
            wiremock.addMockServiceRequestListener(
                    WireMockStubs::requestReceived);
        }
        setupAuthorizationStub();
    }

    protected void setupLauIdamApiStubToReturnMoreRecords() {
        var request = WireMock.get(WireMock.urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH));
        var response = WireMock.aResponse();
        response.withHeader(CONTENT_TYPE, APP_JSON);
        response.withBody("""
            {"deletionLogs": [{
                "userId": "00001",
                "emailAddress": "joe.doe@example.org",
                "firstName": "Joe",
                "lastName": "Doe",
                "deletionTimestamp": "2023-08-23T22:20:05.023Z"
            }],
            "startRecordNumber": 1,
            "moreRecords": true
            }"""
        );
        wiremock.stubFor(request.willReturn(response));
    }

    protected void setupLauApiStubs() {
        wiremock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, APP_JSON)
                        .withBodyFile("lauIdamGetDeletedUsers.json")
                )
        );
    }

    protected void setupApiResponse(UrlPathPattern urlPattern, String bodyAsJson) {
        setupApiResponse(urlPattern, null, bodyAsJson);
    }

    protected void setupApiResponse(UrlPattern urlPattern, Map<String, String> queryParams, String bodyAsJson) {
        MappingBuilder requestStubBuilder = WireMock.get(urlPattern);
        if (queryParams != null) {
            for (Map.Entry<String, String> entry: queryParams.entrySet()) {
                requestStubBuilder.withQueryParam(entry.getKey(), equalTo(entry.getValue()));
            }
        }
        requestStubBuilder.willReturn(WireMock.aResponse().withHeader(CONTENT_TYPE, APP_JSON).withBody(bodyAsJson));
        wiremock.stubFor(requestStubBuilder);
    }

    protected void setupPagedLauApiStubs() {
        String page1 = """
            {
              "deletionLogs": [{
                  "userId":"00001","emailAddress":"a@a.a","firstName":"J","lastName":"D",
                  "deletionTimestamp":"2023-08-23T22:20:05.023Z"
                },{
                  "userId":"00002","emailAddress":"a@a.a","firstName":"J","lastName":"D",
                  "deletionTimestamp":"2023-08-23T20:20:05.023Z"
                }],
              "startRecordNumber": 1,
              "moreRecords": true
            }""";

        String page2 = """
            {
              "deletionLogs": [{
                  "userId":"00003","emailAddress":"a@a.a","firstName":"J","lastName":"D",
                  "deletionTimestamp":"2023-08-23T16:20:05.023Z"
                }],
              "startRecordNumber": 3,
              "moreRecords": false
            }""";

        wiremock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH))
                .withQueryParam("page", equalTo("1"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, APP_JSON)
                        .withBody(page1)
                )
        );
        wiremock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH))
                .withQueryParam("page", equalTo("2"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, APP_JSON)
                        .withBody(page2)
                )
        );

    }

    protected void setupIdamRestoreStub(int httpReturnStatus, String errorDescription) {
        var response = WireMock.aResponse();
        response.withHeader(CONTENT_TYPE, APP_JSON);
        response.withStatus(httpReturnStatus);
        if (errorDescription != null) {
            response.withBody(String.format("""
                {
                    "error":"doesn't matter",
                    "error_description":"%s"
                }
                """, errorDescription));
        }
        var request = WireMock.post(WireMock.urlPathTemplate(Constants.STALE_USERS_PATH + "/{userId}"));
        wiremock.stubFor(request.willReturn(response));
    }

    public void setupIdamApiStubsForSuccess() {
        for (int i = 0; i < 3; i++) {
            // add 25 user ids with uuid
            // "13e31622-edea-493c-8240-9b780c9d6001"
            // changing only the last three bits (001 to 025)
            wiremock.stubFor(
                    WireMock
                            .get(WireMock.urlPathEqualTo(Constants.STALE_USERS_PATH))
                            .withQueryParam("page", equalTo(String.valueOf(i)))
                            .willReturn(
                                    WireMock.aResponse()
                                            .withHeader(CONTENT_TYPE, APP_JSON)
                                            .withBodyFile("staleUsersPage" + (i + 1) + ".json")
                            )
            );

            // pretend that 001, 002, 010 and 023 still have assigned roles
            wiremock.stubFor(
                    WireMock
                            .post(WireMock.urlPathEqualTo(Constants.ROLE_ASSIGNMENTS_QUERY_PATH))
                            .withRequestBody(WireMock.matchingJsonPath(
                                            "$.queryRequests[0].actorId",
                                            WireMock.containing(getMatchingActorId(i + 1))
                                    )
                            )
                            .willReturn(
                                    WireMock.aResponse()
                                            .withBodyFile("roleAssignmentsResponse" + (i + 1) + ".json")
                                            .withHeader(CONTENT_TYPE, Constants.ROLE_ASSIGNMENTS_CONTENT_TYPE)
                            )
            );
        }

        // delete endpoint
        wiremock.stubFor(
                WireMock
                        .delete(WireMock.urlPathMatching(Constants.STALE_USERS_PATH + "/([0-9a-zA-Z-]+)"))
                        .willReturn(WireMock.aResponse().withStatus(OK.value()))
        );
    }

    public void setIdamApiStubToReturn401() {

        String scenarioName = "UNAUTHORIZED";
        String state = "UNAUTHORIZED FIRED";

        wiremock.stubFor(
                WireMock
                        .get(WireMock.urlPathEqualTo(Constants.STALE_USERS_PATH))
                        .inScenario(scenarioName)
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willSetStateTo(state)
                        .willReturn(
                                WireMock.unauthorized()
                        )
        );

        String body = "{\"content\": [], \"totalPages\": 1, \"totalElements\": 0, \"last\": true}";
        wiremock.stubFor(
                WireMock
                        .get(WireMock.urlPathEqualTo(Constants.STALE_USERS_PATH))
                        .inScenario(scenarioName)
                        .whenScenarioStateIs(state)
                        .willReturn(
                                WireMock.jsonResponse(body, 200)
                        )
        );
    }

    public void setIdamApiStubToReturn500() {
        wiremock.stubFor(
                WireMock
                        .get(WireMock.urlPathEqualTo(Constants.STALE_USERS_PATH))
                        .willReturn(
                                WireMock.serverError()
                        )
        );
    }

    public void setIdamApiStubToReturnErrorOnEndpoint(int errorCode, String endpointPattern) {
        wiremock.stubFor(
            WireMock
                .delete(WireMock.urlPathMatching(endpointPattern))
                .willReturn(WireMock.status(errorCode))
        );
    }

    protected static void requestReceived(Request request, Response response) {
        log.trace("WireMock request {} at URL: {}", request.getMethod(), request.getAbsoluteUrl());
        log.trace("WireMock request headers: \n{}", request.getHeaders());
        log.trace("WireMock response status: {}", response.getStatus());
        log.trace("WireMock response body: \n{}", response.getBodyAsString());
        log.trace("WireMock response headers: \n{}", response.getHeaders());
    }

    private String getMatchingActorId(int page) {
        // UUID is from wiremock/__files/staleUsersPage{1,2,3}.json
        return "13e31622-edea-493c-8240-9b780c9d60" + (page - 1) + "1";
    }


    private void setupAuthorizationStub() {
        wiremock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/details"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                                        .withStatus(200)
                                        .withBody("disposer")
                        )
        );

        wiremock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/o/userinfo"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                                        .withStatus(200)
                                        .withBody(getUserInfoAsJson())
                        )
        );

        String jsonBody = "{\"access_token\": \"" + dummyJwtToken + "\"}";

        wiremock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/o/token"))
                        .willReturn(WireMock.jsonResponse(jsonBody, 200))
        );

        wiremock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/lease"))
                        .willReturn(WireMock.aResponse().withBody(dummyJwtToken))
        );
    }

    private String getUserInfoAsJson() {
        return new Gson().toJson(
                new UserInfo(
                        "sub",
                        "uid",
                        "test",
                        "given_name",
                        "family_Name",
                        List.of("cft-audit-investigator")
                )
        );
    }


}
