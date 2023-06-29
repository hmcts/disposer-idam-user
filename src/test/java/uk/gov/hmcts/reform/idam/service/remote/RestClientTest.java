package uk.gov.hmcts.reform.idam.service.remote;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.requests.RequestBody;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.UserRoleAssignmentQueryRequests;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class RestClientTest {
    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ParameterResolver idamConfig;

    @Test
    void shouldExecuteGetRequest() {
        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);
        final Invocation.Builder builder = mock(Invocation.Builder.class);
        final Response response = mock(Response.class);

        final RestClient restClient = new RestClient(securityUtil, idamConfig);

        setField(restClient, "client", client);

        doReturn("Bearer 1234").when(securityUtil).getIdamClientToken();
        doReturn("Service 5678").when(securityUtil).getServiceAuthorization();
        when(client.target(any(String.class))).thenReturn(webTarget);
        when(webTarget.path(any(String.class))).thenReturn(webTarget);
        when(webTarget.queryParam(any(String.class), any(String.class))).thenReturn(webTarget);

        when(webTarget.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder);
        when(builder.header("Authorization", "Bearer 1234")).thenReturn(builder);
        when(builder.header("ServiceAuthorization", "Service 5678")).thenReturn(builder);
        when(builder.get()).thenReturn(response);

        restClient.getRequest(
            "http://localhost:9090",
            "/delete",
            Map.of("size", 100, "sort", "log-timestamp")
        );

        verify(webTarget, times(2)).queryParam(any(String.class), any(Object.class));
        verify(webTarget, times(1)).request(MediaType.APPLICATION_JSON_TYPE);
        verify(builder, times(1)).get();
    }

    @Test
    void shouldExecutePostRequest() {
        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);
        final Invocation.Builder builder = mock(Invocation.Builder.class);
        final Response response = mock(Response.class);

        final RestClient restClient = new RestClient(securityUtil, idamConfig);

        setField(restClient, "client", client);

        doReturn("Bearer 1234").when(securityUtil).getIdamClientToken();
        doReturn("Service 5678").when(securityUtil).getServiceAuthorization();
        when(client.target(any(String.class))).thenReturn(webTarget);
        when(webTarget.path(any(String.class))).thenReturn(webTarget);

        when(webTarget.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder);
        when(builder.header("Authorization", "Bearer 1234")).thenReturn(builder);
        when(builder.header("ServiceAuthorization", "Service 5678")).thenReturn(builder);
        when(builder.header(eq("Content-Type"), anyString())).thenReturn(builder);

        var headers = Map.of("Content-Type", "Application/json");

        List<String> staleUsers = List.of("1", "2");
        var roleAssignmentQuery = UserRoleAssignmentQueryRequest.builder().userIds(staleUsers).build();
        RequestBody body = UserRoleAssignmentQueryRequests.builder().queryRequests(roleAssignmentQuery).build();
        when(builder.post(Entity.json(body))).thenReturn(response);

        restClient.postRequest(
            "http://localhost:9090",
            "/delete",
            headers,
            body
        );

        verify(webTarget, times(1)).request(MediaType.APPLICATION_JSON_TYPE);
        verify(builder, times(3)).header(anyString(), anyString());
        verify(builder, times(1)).post(Entity.json(body));
    }

    @Test
    void shouldGetClient() {
        int timeout = 60_000;
        when(idamConfig.getReadTimeout()).thenReturn(timeout);
        when(idamConfig.getConnectTimeout()).thenReturn(timeout);

        final RestClient restClient = new RestClient(securityUtil, idamConfig);

        var client = restClient.getClient();
        assertThat(client.getConfiguration().getProperty(READ_TIMEOUT)).isEqualTo(timeout);
        assertThat(client.getConfiguration().getProperty(CONNECT_TIMEOUT)).isEqualTo(timeout);
    }

}
