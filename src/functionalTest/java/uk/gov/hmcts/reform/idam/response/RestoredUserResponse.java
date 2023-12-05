package uk.gov.hmcts.reform.idam.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class RestoredUserResponse {

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "username")
    private String username;

    @JsonProperty(value = "fullObject")
    private String fullObject;

    @JsonProperty(value = "creationDate")
    private Timestamp creationDate;

    @JsonProperty(value = "last_login_date")
    private Timestamp lastLoginDate;
}
