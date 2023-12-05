package uk.gov.hmcts.reform.idam.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestoredUserFullObject {

    @JsonProperty("givenName")
    private String forename;

    @JsonProperty("sn")
    private String surname;

    @JsonProperty("mail")
    private String email;

    @JsonProperty
    private String userName;
}
