package uk.gov.hmcts.reform.idam.service.remote;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.requests.RequestBody;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.Map;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

@Service
@Getter
public class RestClient {
    private final SecurityUtil securityUtil;
    private final RestConfig restConfig;
    private final Client client;

    public RestClient(final SecurityUtil securityUtil, final RestConfig restConfig) {
        this.securityUtil = securityUtil;
        this.restConfig = restConfig;
        this.client = buildClient();
    }

    public Response getRequest(final String host, final String path, final Map<String, Object> queryParams) {
        return getRequest(host, path, null, queryParams);
    }

    public Response getRequest(
        final String host,
        final String path,
        final Map<String, String> headers,
        final Map<String, Object> queryParams) {

        final var target = createWebTarget(host, path, headers, queryParams);

        return target.get();
    }

    public Response postRequest(final String host, final String path, Map<String, String> headers, RequestBody body) {
        final var target = createWebTarget(host, path, headers, null);
        return target.post(Entity.json(body));
    }

    private Invocation.Builder createWebTarget(
        final String host,
        final String path,
        final Map<String, String> headers,
        final Map<String, Object> queryParams
    ) {

        WebTarget target = client.target(host).path(path);

        if (queryParams != null) {
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue().toString());
            }
        }

        Invocation.Builder targetBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                targetBuilder = targetBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        return targetBuilder
           .header(restConfig.getAuthorisationHeaderName(), securityUtil.getIdamClientToken())
           .header(restConfig.getServiceAuthorisationHeaderName(), securityUtil.getServiceAuthorization());
    }

    private Client buildClient() {
        final ClientConfig clientConfig = new ClientConfig()
            .property(READ_TIMEOUT, restConfig.getReadTimeout())
            .property(CONNECT_TIMEOUT, restConfig.getConnectTimeout());

        return ClientBuilder
            .newClient(clientConfig)
            .register(new LoggingFeature());
    }
}
