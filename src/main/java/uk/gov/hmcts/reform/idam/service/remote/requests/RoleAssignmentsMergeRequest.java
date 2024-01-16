package uk.gov.hmcts.reform.idam.service.remote.requests;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RoleAssignmentsMergeRequest {
    private RoleRequest roleRequest;
    private List<RequestedRole> requestedRoles;
}
