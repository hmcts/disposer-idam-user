package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserRoleAssignmentQueryRequests implements RequestBody {

    @JsonProperty("queryRequests")
    UserRoleAssignmentQueryRequest queryRequests;

}
