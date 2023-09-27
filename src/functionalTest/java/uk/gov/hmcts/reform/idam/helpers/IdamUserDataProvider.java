package uk.gov.hmcts.reform.idam.helpers;

import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class IdamUserDataProvider {

    private final IdamTokenGenerator idamTokenGenerator;

    @Value("${idam.api.url}")
    private String idamApi = "https://idam-api.aat.platform.hmcts.net";

    @Value("${stale-users.roles}")
    private String roleToDelete;

    private static final String CREATE_USER_PATH = "testing-support/accounts";
    private static final String RETIRE_USER_PATH_TMPL = "api/v1/staleUsers/%s/retire";

    public String setup() {
        RestAssured.baseURI = idamApi;
        String userId = createUser();
        retireUser(userId);
        return userId;
    }

    private String createUser() {
        return RestAssured.given()
            .header("Authorization", idamTokenGenerator.getIdamAuthorizationHeader())
            .header("Content-Type", "application/json")
            .body(makeUser())
            .when()
            .post(CREATE_USER_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    }

    private void retireUser(String userId) {
        RestAssured.given()
            .header("Authorization", idamTokenGenerator.getIdamAuthorizationHeader())
            .header("Content-Type", "application/json")
            .post(String.format(RETIRE_USER_PATH_TMPL, userId))
            .then()
            .statusCode(200);
    }

    private String makeUser() {
        JSONObject user = new JSONObject();
        try {
            String name = UUID.randomUUID().toString();
            user.put("email", "DisposerTest-" + name + "@example.org");
            user.put("forename", "Lau " + name);
            user.put("surname", "Test");
            user.put("password", "{Pass12345Y");
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
