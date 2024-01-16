package uk.gov.hmcts.reform.idam.requests;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RoleAssignmentAttributes {
    private String substantive;
    private String caseId;
    private String jurisdiction;
    private String caseType;
}
