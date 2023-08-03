package uk.gov.hmcts.reform.idam.parameter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class DefaultParameterResolver implements ParameterResolver {

    @Value("${idam.api.url}")
    private String idamHost;

    @Value("${idam.api.username}")
    private String idamUsername;

    @Value("${idam.api.password}")
    private String idamPassword;

    @Value("${idam.client.batch_size}")
    private int batchSize;

    @Value("${idam.client.requests_limit}")
    private int requestsLimit;

}
