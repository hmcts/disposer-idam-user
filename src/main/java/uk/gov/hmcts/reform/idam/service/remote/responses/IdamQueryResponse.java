package uk.gov.hmcts.reform.idam.service.remote.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdamQueryResponse {
    private String id;
    private String forename;
    private String surname;
    private String email;
    private boolean active;
    private boolean locked;
    private boolean stale;
    private List<String> roles;

}
