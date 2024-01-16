package uk.gov.hmcts.reform.idam.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class QueryRequest {
    private List<String> actorId;
}
