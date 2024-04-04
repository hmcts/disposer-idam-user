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
            {"queryRequests":[{"actorId":["user1","user2"]}],"size":100}
            """.strip();
        var staleUsers = List.of("user1", "user2");
        var request = new RoleAssignmentsQueryRequest(staleUsers,10);
        ObjectMapper mapper = new ObjectMapper();
        String requestValue = mapper.writeValueAsString(request);
        assertThat(mapper.writeValueAsString(request))
            .isEqualTo(expected);
    }
}
