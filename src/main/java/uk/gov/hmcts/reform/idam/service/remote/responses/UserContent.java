package uk.gov.hmcts.reform.idam.service.remote.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserContent {
    private String id;
    private List<String> roles;
}
