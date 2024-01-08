package uk.gov.hmcts.reform.idam.service.remote.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RoleAssignmentAttributes {
    String substantive;
    String caseId;
    String jurisdiction;
    String caseType;
}
