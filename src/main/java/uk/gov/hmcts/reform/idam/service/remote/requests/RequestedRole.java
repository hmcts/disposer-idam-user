package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentAttributes;

import java.time.Instant;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant beginTime;
    private String process;
    private RoleAssignmentAttributes attributes;

}
