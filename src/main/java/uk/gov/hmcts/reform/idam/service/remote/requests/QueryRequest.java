package uk.gov.hmcts.reform.idam.service.remote.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class QueryRequest {

    @JsonProperty("actorId")
    private List<String> userIds;
}
