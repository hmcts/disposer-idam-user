package uk.gov.hmcts.reform.idam.requests;

import lombok.Builder;

@Builder
public class RoleRequest {
    private String requestType;

    private String process;

    private String reference;

    private String assignerId;

    private boolean replaceExisting;
}
