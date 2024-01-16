package uk.gov.hmcts.reform.idam.service.remote.requests;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RoleRequest {

    private String requestType;

    private String process;

    private String reference;

    private String assignerId;

    private boolean replaceExisting;
}
