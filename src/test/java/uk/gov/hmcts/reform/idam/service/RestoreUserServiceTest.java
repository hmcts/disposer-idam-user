package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RestoreUserRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("PMD.LawOfDemeter")
@ExtendWith(MockitoExtension.class)
class RestoreUserServiceTest {

    @Mock
    IdamTokenGenerator idamTokenGenerator;

    @Mock
    IdamClient idamClient;

    @Mock
    Response response;

    @Captor
    ArgumentCaptor<RestoreUserRequest> requestCaptor;

    @InjectMocks
    private RestoreUserService service;

    @Test
    void restoreUserShouldCallIdamClientToRestoreUser() {
        String userId = "00001";
        final DeletionLog log = createDeletionLog(userId);
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer 01");
        when(response.status()).thenReturn(HttpStatus.CREATED.value());
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        assertThat(service.restoreUser(log)).isTrue();
        verify(idamClient, times(1)).restoreUser(anyString(), eq(userId), requestCaptor.capture());
        RestoreUserRequest request = requestCaptor.getValue();
        assertThat(request.getId()).isEqualTo("00001");
        assertThat(request.getEmail()).isEqualTo(log.getEmailAddress());
        assertThat(request.getFirstName()).isEqualTo(log.getFirstName());
        assertThat(request.getLastName()).isEqualTo(log.getLastName());
        assertThat(request.getRoles()).hasSize(1);
        assertThat(request.getRoles().get(0)).isEqualTo("citizen");
    }

    @Test
    void restoreUserReturnsTrueOn201() {
        final DeletionLog log = createDeletionLog("0001");
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer 01");

        when(response.status()).thenReturn(HttpStatus.CREATED.value());
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);
        assertThat(service.restoreUser(log)).isTrue();
    }

    @Test
    void restoreUserReturnsTrueOn409IdError() throws IOException {
        final DeletionLog log = createDeletionLog("0001");
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer 01");
        when(response.status()).thenReturn(HttpStatus.CONFLICT.value());

        mockBodyWithMessage("id in use");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);
        assertThat(service.restoreUser(log)).isTrue();

        mockBodyWithMessage("id already archived");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);
        assertThat(service.restoreUser(log)).isTrue();

        mockBodyWithMessage("ID IN USE");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);
        assertThat(service.restoreUser(log)).isTrue();
    }

    @Test
    void restoreUserReturnsFalseOnNon201() throws IOException {
        final DeletionLog log = createDeletionLog("0001");
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer 01");
        mockBodyWithMessage("email in use");

        when(response.status())
            .thenReturn(HttpStatus.FORBIDDEN.value())
            .thenReturn(HttpStatus.CONFLICT.value())
            .thenReturn(HttpStatus.NO_CONTENT.value())
            .thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .thenReturn(HttpStatus.BAD_GATEWAY.value())
            .thenReturn(HttpStatus.GATEWAY_TIMEOUT.value())
            .thenReturn(HttpStatus.REQUEST_TIMEOUT.value());

        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        assertThat(service.restoreUser(log)).isFalse();
        assertThat(service.restoreUser(log)).isFalse();
        assertThat(service.restoreUser(log)).isFalse();
        assertThat(service.restoreUser(log)).isFalse();
        assertThat(service.restoreUser(log)).isFalse();
        assertThat(service.restoreUser(log)).isFalse();
        assertThat(service.restoreUser(log)).isFalse();
    }

    private void mockBodyWithMessage(String message) throws IOException {
        final Response.Body body = mock(Response.Body.class);
        String json = String.format("""
            {
                "error":"doesn't matter",
                "error_description":"%s"
            }
            """, message);
        when(body.asInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));
        when(response.body()).thenReturn(body);
    }

    private DeletionLog createDeletionLog(String userId) {
        return DeletionLog.builder()
            .userId(userId)
            .emailAddress("joe.doe@example.org")
            .firstName("Joe")
            .lastName("Doe")
            .deletionTimestamp("2023-11-02T00:03:23")
            .build();
    }
}
