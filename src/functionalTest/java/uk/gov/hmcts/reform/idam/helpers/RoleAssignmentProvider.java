package uk.gov.hmcts.reform.idam.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.ServiceTokenGenerator;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleAssignmentProvider {

    private final IdamTokenGenerator idamTokenGenerator;
    private final ServiceTokenGenerator serviceTokenGenerator;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ccd.role.assignment.host}")
    private String roleAssignmentApi;

    private static final String ROLE_PATH = "/am/role-assignments";

    public void setup(String userId) {
        RestAssured.baseURI = roleAssignmentApi;
        assignRole(userId);
    }

    public void deleteRole() {
        RestAssured.given()
            .header("Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader())
            .header("ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken())
            .param("process", "businessProcess1")
            .param("reference", "ed474902-05f8-4358-bafb-b3afb0cc5d57")
            .when()
            .delete(ROLE_PATH)
            .then()
            .statusCode(204);
    }

    public void assignRole(String userId) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            JsonNode role = mapper.readTree(classLoader.getResourceAsStream("role-assignment.json"));
            JsonNode requestedRole = role.get("requestedRoles").get(0);
            ((ObjectNode)requestedRole).put("actorId", userId);
            RestAssured.given()
                .header("Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader())
                .header("ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken())
                .header("Content-Type", "application/json")
                .body(role)
                .when()
                .post(ROLE_PATH)
                .then()
                .statusCode(201);
        } catch (IOException exc) {
            log.error(exc.getMessage(), exc);
        }
    }
}
