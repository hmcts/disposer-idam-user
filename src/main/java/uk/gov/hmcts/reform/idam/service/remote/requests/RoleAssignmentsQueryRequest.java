package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleAssignmentsQueryRequest {

    @JsonProperty("queryRequests")
    private final List<QueryRequest> queryRequests;

    public RoleAssignmentsQueryRequest(List<String> userIds) {
        queryRequests = List.of(QueryRequest.builder().userIds(userIds).build());
    }
}
