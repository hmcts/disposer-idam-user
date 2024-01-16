package uk.gov.hmcts.reform.idam.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import uk.gov.hmcts.reform.idam.requests.RoleAssignmentAttributes;

import java.time.Instant;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignment {
    String actorIdType;
    String actorId;
    String roleType;
    String roleName;
    String classification;
    String grantType;
    String roleCategory;
    boolean readOnly;
    Instant beginTime;
    RoleAssignmentAttributes attributes;
}
