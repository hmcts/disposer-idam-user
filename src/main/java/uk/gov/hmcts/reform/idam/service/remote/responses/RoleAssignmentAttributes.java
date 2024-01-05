package uk.gov.hmcts.reform.idam.service.remote.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleAssignmentAttributes {
    String substantive;
    String caseId;
    String jurisdiction;
    String caseType;
}
