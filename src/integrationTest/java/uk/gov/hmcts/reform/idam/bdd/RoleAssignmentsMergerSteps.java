package uk.gov.hmcts.reform.idam.bdd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jknack.handlebars.internal.Files;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.idam.service.IdamDuplicateUserMergerService;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.service.remote.responses.IdamQueryResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Slf4j
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class RoleAssignmentsMergerSteps extends WireMockStubs {

    @Autowired
    private IdamDuplicateUserMergerService userMergerService;

    @Before
    public void resetWiremock() {
        wiremock.resetAll();
        setupWireMock();
    }

    @Given("lau-idam fetch users returns")
    public void lauIdamFetchUsersReturns(DataTable dataTable) throws JsonProcessingException {
        var data = createDeletionLogs(dataTable.asMaps());
        DeletedUsersResponse deletedUsersResponse = DeletedUsersResponse.builder()
            .deletionLogs(data)
            .moreRecords(false)
            .startRecordNumber(1)
            .build();
        UrlPathPattern urlPattern = urlPathEqualTo(Constants.LAU_GET_DELETED_USERS_PATH);
        setupApiResponse(urlPattern, toJson(deletedUsersResponse));
    }

    @Given("IdAM fetch user by email returns")
    public void idamFetchUserByEmailReturns(DataTable dataTable) throws JsonProcessingException {
        UrlPathPattern urlPattern = urlPathEqualTo(Constants.IDAM_QUERY_PATH);
        for (Map<String, String> dataEntry: dataTable.asMaps()) {
            var data = createIdamQueryResponse(dataEntry);
            Map<String, String> queryParams = Map.of(
                "query", "email:" + data.getEmail()
            );
            setupApiResponse(urlPattern, queryParams, toJson(List.of(data)));
        }
    }

    @Given("role-assignments fetch role assignments for user {string} returns")
    public void roleAssignmentsFetchRoleAssignmentsReturns(
        String userId, DataTable dataTable
    ) throws JsonProcessingException {
        UrlPathPattern urlPattern = WireMock.urlPathTemplate(Constants.ROLE_ASSIGNMENTS_ACTOR_PATH);
        var data = createRoleAssignmentsResponse(dataTable.asMaps());
        setupApiResponse(urlPattern, toJson(data));
    }

    @When("merge service runs")
    public void runMergeService() {
        userMergerService.run();
    }

    @Then("it should make POST request to {string} with data as in {string}")
    public void itShouldMakePostRequestToWithDataAsIn(
        String url, String dataFilePath
    ) throws URISyntaxException, IOException {
        URL resource = RoleAssignmentsMergerSteps.class.getClassLoader().getResource(dataFilePath);
        File file = Paths.get(resource.toURI()).toFile();
        String body = Files.read(file, Charset.defaultCharset());
        wiremock.verify(1, postRequestedFor(urlPathEqualTo(url)).withRequestBody(equalToJson(body)));
    }

    private List<DeletionLog> createDeletionLogs(List<Map<String, String>> data) {
        return data.stream().map(log -> DeletionLog.builder()
            .userId(log.get("userId"))
            .emailAddress(log.get("email"))
            .firstName(log.get("first name"))
            .lastName(log.get("last name"))
            .deletionTimestamp(log.get("deletion timestamp"))
            .build())
            .toList();
    }

    private IdamQueryResponse createIdamQueryResponse(Map<String, String> dataEntry) {
        IdamQueryResponse response = new IdamQueryResponse();
        response.setId(dataEntry.get("id"));
        response.setEmail(dataEntry.get("email"));
        return response;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private RoleAssignmentResponse createRoleAssignmentsResponse(
        List<Map<String, String>> dataList
    ) throws JsonProcessingException {
        RoleAssignmentResponse response = new RoleAssignmentResponse();

        List<RoleAssignment> roleAssignments = new LinkedList<>();
        for (Map<String, String> mapping: dataList) {
            RoleAssignment roleAssignment = new RoleAssignment();
            for (Map.Entry<String, String> entry: mapping.entrySet()) {
                set(roleAssignment, entry.getKey(), entry.getValue());
            }
            roleAssignment.setReadOnly(Boolean.getBoolean(mapping.get("readOnly")));
            roleAssignment.setBeginTime(Instant.parse(mapping.get("beginTime")));
            roleAssignment.setAttributes(createRoleAssignmentsAttributes(mapping.get("attributes")));
            roleAssignments.add(roleAssignment);
        }
        response.setRoleAssignments(roleAssignments);
        return response;
    }

    private RoleAssignmentAttributes createRoleAssignmentsAttributes(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RoleAssignmentAttributes.class);
    }

    private String toJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(object);
    }

    private void set(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        try {
            Method setter = clazz.getDeclaredMethod("set" + StringUtils.capitalise(fieldName), String.class);
            setter.invoke(object, fieldValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignoring
            log.error(e.getMessage());
        }
    }

}
