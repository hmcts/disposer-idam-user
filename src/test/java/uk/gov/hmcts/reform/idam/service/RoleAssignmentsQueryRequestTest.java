package uk.gov.hmcts.reform.idam.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsQueryRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAssignmentsQueryRequestTest {
    @Test
    void shouldSerialiseToJson() throws JsonProcessingException {
        String expected = """
            {"queryRequests":[{"actorId":["user1","user2"]}]}
            """.strip();
        List<String> staleUsers = List.of("user1", "user2");
        RoleAssignmentsQueryRequest request = new RoleAssignmentsQueryRequest(staleUsers);
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.writeValueAsString(request))
            .isEqualTo(expected);
    }
}
