package uk.gov.hmcts.reform.idam.parameter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ParameterResolver {

    @Value("${idam.api.url}")
    private String idamHost;

    @Value("${stale-users.batch.size}")
    private int batchSize;

    @Value("${idam.client.id}")
    private String clientId;

    @Value("${idam.client.secret}")
    private String clientSecret;


}
