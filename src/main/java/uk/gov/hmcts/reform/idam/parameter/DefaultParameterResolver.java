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

    @Value("${idam.client.role_assignments_content_type}")
    private String roleAssignmentsContentType;

    @Value("${idam.client.batch_size}")
    private int batchSize;

    @Value("${idam.client.requests_limit}")
    private int requestsLimit;

    @Value("${idam.client.read_timeout}")
    private int readTimeout;

    @Value("${idam.client.connect_timeout}")
    private int connectTimeout;

    @Value("${idam.client.stale_users_path}")
    private String staleUsersPath;

    @Value("${idam.client.role_assignments_path}")
    private String roleAssignmentsPath;

}
