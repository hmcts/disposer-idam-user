package uk.gov.hmcts.reform.idam.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleAssignmentsQueryRequest {

    @JsonProperty("queryRequests")
    private List<QueryRequest> queryRequests;

    public RoleAssignmentsQueryRequest(List<String> userIds) {
        queryRequests = List.of(QueryRequest.builder().actorId(userIds).build());
    }
}
