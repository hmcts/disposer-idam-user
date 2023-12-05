package uk.gov.hmcts.reform.idam.service.remote.requests;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RestoreUserRequest {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roles;
}
