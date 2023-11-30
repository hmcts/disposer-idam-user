package uk.gov.hmcts.reform.idam.service.remote.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdamCreateUserErrorResponse {

    @JsonProperty("error_description")
    private String errorDescription;

}
