package uk.gov.hmcts.reform.idam.requests;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestedRole {
    private String actorIdType;
    private String actorId;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private boolean readOnly;
    private String beginTime;
    private String process;
    private RoleAssignmentAttributes attributes;

}
