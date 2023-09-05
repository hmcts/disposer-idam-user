package uk.gov.hmcts.reform.idam.parameter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ParameterResolver {

    @Value("${idam.api.url}")
    private String idamHost;

    @Value("${idam.s2s-auth.secret}")
    private String s2sSecret;

    @Value("${stale-users.batch.size}")
    private int batchSize;

    @Value("${stale-users.requests.limit}")
    private int requestsLimit;

}
