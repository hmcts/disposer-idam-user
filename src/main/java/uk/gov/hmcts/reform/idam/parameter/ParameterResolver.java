package uk.gov.hmcts.reform.idam.parameter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@Getter
@Setter
public class ParameterResolver {

    @Value("${idam.api.url}")
    private String idamHost;

    @Value("${stale-users.batch.size}")
    private int batchSize;

    @Value("${stale-users.requests.limit}")
    private int requestLimit;

    @Value("${idam.client.id}")
    private String clientId;

    @Value("${idam.client.secret}")
    private String clientSecret;

    @Value("${stale-users.required_role}")
    private String requiredRole;

    @Value("${stale-users.citizen_roles:#{null}}")
    private Optional<Set<String>> idamRolesToDelete;

    @Value("${idam.client.username}")
    private String clientUserName;

    @Value("${idam.client.password}")
    private String clientPassword;

    @Value("${idam.client.redirect_uri}")
    private String redirectUri;

    @Value("${stale-users.simulation.mode}")
    private Boolean isSimulation;
}
