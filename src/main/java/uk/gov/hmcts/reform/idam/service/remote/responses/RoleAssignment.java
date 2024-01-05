package uk.gov.hmcts.reform.idam.service.remote.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
