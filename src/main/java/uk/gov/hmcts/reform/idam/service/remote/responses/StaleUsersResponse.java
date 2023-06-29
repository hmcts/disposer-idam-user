package uk.gov.hmcts.reform.idam.service.remote.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaleUsersResponse {
    @JsonProperty("staleUsers")
    private List<String> staleUsers;

    @JsonProperty("moreRecords")
    private Boolean moreRecords;

}
