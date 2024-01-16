package uk.gov.hmcts.reform.idam.service.remote.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentAttributes {

    private String substantive;
    private String caseId;
    private String jurisdiction;
    private String caseType;
}
