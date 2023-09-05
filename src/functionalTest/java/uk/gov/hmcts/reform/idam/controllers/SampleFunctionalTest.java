package uk.gov.hmcts.reform.idam.controllers;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource("classpath:application-functional.yaml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class SampleFunctionalTest {
    protected static final String CONTENT_TYPE_VALUE = "application/json";

    @Value("${TEST_URL:http://localhost:8080}")
    private String testUrl;

    @Value("${DISPOSER_IDAM_USER_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${S2S_SECRET_DISPOSER_IDAM_USER}")
    private String s2sSecret;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void functionalTest() {
        /*Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .get()
            .then()
            .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.asString().startsWith("Welcome"));*/
        Assertions.assertNotNull(clientSecret);
        Assertions.assertNotEquals("idam-client-secret", clientSecret);
        Assertions.assertNotNull(s2sSecret);
        Assertions.assertNotEquals("AAAAAAAAAAAAAAAA", clientSecret);
    }
}
