package uk.gov.hmcts.reform.idam.helpers;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class IdamUserDataProvider {

    private final IdamTokenGenerator idamTokenGenerator;

    private final SecurityUtil securityUtil;

    @Value("${idam.api.url}")
    private String idamApi;

    @Value("${stale-users.roles}")
    private String roleToDelete;

    private static final String CREATE_USER_PATH = "testing-support/accounts";
    private static final String RETIRE_USER_PATH_TMPL = "api/v1/staleUsers/%s/retire";

    public String setup() {
        securityUtil.generateTokens();

        String name = UUID.randomUUID().toString();
        String email = "DisposerRestorer-@example.org";
        String[] emailParts = email.split("-");
        email = emailParts[0] + name + emailParts[1];
        String foreName = "Lau " + name;

        ExtractableResponse<Response> res = createUser(email, foreName, "Test", "{Pass12345Y");
        String userId = res.path("id");
        retireUser(userId);
        return userId;
    }

    private ExtractableResponse<Response> createUser(String email, String foreName, String lastName, String password) {
        return RestAssured.given()
            .header("Authorization", idamTokenGenerator.getIdamAuthorizationHeader())
            .header("Content-Type", "application/json")
            .body(makeUser(email, foreName, lastName, password))
            .baseUri(idamApi)
            .when()
            .post(CREATE_USER_PATH)
            .then()
            .statusCode(201)
            .extract();
    }

    private void retireUser(String userId) {
        RestAssured.given()
            .header("Authorization", idamTokenGenerator.getIdamAuthorizationHeader())
            .header("Content-Type", "application/json")
            .baseUri(idamApi)
            .post(String.format(RETIRE_USER_PATH_TMPL, userId))
            .then()
            .statusCode(200);
    }

    private String makeUser(String email, String foreName, String lastName, String password) {
        JSONObject user = new JSONObject();
        try {
            user.put("email", email);
            user.put("forename", foreName);
            user.put("surname", lastName);
            user.put("password", password);
            JSONArray roles = new JSONArray();
            JSONObject role = new JSONObject();
            role.put("code", roleToDelete);
            roles.put(role);
            user.put("roles", roles);
        } catch (JSONException je) {
            log.error(je.getMessage(), je);
        }

        return user.toString();
    }

}
