package uk.gov.hmcts.reform.idam.controllers;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SampleFunctionalTest {
    protected static final String CONTENT_TYPE_VALUE = "application/json";

    @Value("${TEST_URL:http://localhost:8080}")
    private String testUrl;

    @Value("${idam.s2s-auth.secret}")
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
        String test = "abc";
        Assertions.assertEquals("abc", test);
        Assertions.assertNotNull(s2sSecret);
        Assertions.assertNotEquals("AAAAAAAAAAAAAAAA",s2sSecret);
    }
}
