package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleAssignmentsQueryRequest {

    @JsonProperty("queryRequests")
    private List<QueryRequest> queryRequests;

    @JsonProperty("size")
    private int roleAssignmentsSize;

    public RoleAssignmentsQueryRequest(List<String> userIds, int size) {
        queryRequests = List.of(QueryRequest.builder().userIds(userIds).build());
        roleAssignmentsSize = size * 10;
    }
}
