package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleAssignmentsPostRequest {

    @JsonProperty("queryRequests")
    private List<QueryRequest> queryRequests;

    public RoleAssignmentsPostRequest(List<String> userIds) {
        queryRequests = List.of(QueryRequest.builder().userIds(userIds).build());
    }
}
