package uk.gov.hmcts.reform.idam.requests;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RoleAssignmentsAssignRoleRequest {
    private RoleRequest roleRequest;
    private List<RequestedRole> requestedRoles;
}
