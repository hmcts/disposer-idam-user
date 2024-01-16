package uk.gov.hmcts.reform.idam.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleAssignmentResponse {
    @JsonProperty("roleAssignmentResponse")
    private List<RoleAssignment> roleAssignments;
}
