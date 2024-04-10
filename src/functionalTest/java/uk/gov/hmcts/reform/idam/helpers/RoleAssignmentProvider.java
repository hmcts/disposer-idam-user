package uk.gov.hmcts.reform.idam.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.exception.UserDetailsGenerationException;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.ServiceTokenGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    public void deleteRoles(List<String> roleReferences) {
        roleReferences.forEach(reference ->
            RestAssured.given()
                .header("Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader())
                .header("ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken())
                .param("process", "businessProcess1")
                .param("reference", reference)
                .when()
                .delete(ROLE_PATH)
                .then()
                .statusCode(204)
        );
    }

    public void assignRole(List<String> userIds) {
        for (String userId: userIds) {
            assignRole(userId);
        }
    }

    public void assignRole(String userId) {
        RestAssured.given()
            .header("Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader())
            .header("ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken())
            .header("Content-Type", "application/json")
            .body(createRoleBody(userId))
            .when()
            .post(ROLE_PATH)
            .then()
            .statusCode(201);
    }

    private JsonNode createRoleBody(String userId) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("role-assignment.json")) {
            JsonNode role = mapper.readTree(is);

            JsonNode roleRequest = role.get("roleRequest");
            ((ObjectNode)roleRequest).put("reference", userId);

            JsonNode requestedRole = role.get("requestedRoles").get(0);
            ((ObjectNode)requestedRole).put("actorId", userId);

            return role;
        } catch (IOException ioe) {
            log.error("Exception reading role-assignment.json template", ioe);
            throw new UserDetailsGenerationException("Failed to read role-assignment.json template", ioe);
        }
    }
}
