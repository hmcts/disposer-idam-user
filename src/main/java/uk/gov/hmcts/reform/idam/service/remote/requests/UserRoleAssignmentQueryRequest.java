package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserRoleAssignmentQueryRequest {

    @JsonProperty("actorId")
    List<String> userIds;
}
