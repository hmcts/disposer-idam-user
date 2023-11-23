package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.service.remote.client.LauIdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.ServiceTokenGenerator;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@ExtendWith(MockitoExtension.class)
class LauIdamUserServiceTest {

    @Mock
    IdamTokenGenerator idamTokenGenerator;

    @Mock
    ServiceTokenGenerator serviceTokenGenerator;

    @Mock
    LauIdamClient lauClient;

    @Mock
    Response response;

    @Captor
    ArgumentCaptor<String> queryCaptor;

    @InjectMocks
    LauIdamUserService service;

    @BeforeEach
    void setUp() {
        when(idamTokenGenerator.getPasswordTypeAuthorizationHeader()).thenReturn("Authorization: Bearer 123");
        when(serviceTokenGenerator.getServiceAuthToken()).thenReturn("Bearer Service");
    }

    @Test
    void fetchDeletedUsersShouldCallLauService() {
        when(lauClient.getDeletedUsers(anyMap(), anyInt())).thenReturn(makeDeletedUsersResponse());
        List<DeletionLog> deletedUsers = service.fetchDeletedUsers(10);
        assertThat(deletedUsers).hasSize(1);
        assertThat(service.hasMore()).isFalse();
        verify(lauClient, times(1)).getDeletedUsers(anyMap(), anyInt());
    }

    @Test
    void deleteLogEntryShouldCallLauService() {
        String userId = "00001";
        when(response.status()).thenReturn(NO_CONTENT.value());
        when(lauClient.deleteLogEntry(anyMap(), eq(userId))).thenReturn(response);
        service.deleteLogEntry(userId);
        verify(lauClient, times(1)).deleteLogEntry(anyMap(), queryCaptor.capture());
        assertThat(queryCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    void fetchDeletedUsersShouldReturnEmptyListOnException() {
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        byte[] body = {};

        when(lauClient.getDeletedUsers(anyMap(), anyInt()))
            .thenThrow(new FeignException.GatewayTimeout("Unauthorized", request, body, null));
        List<DeletionLog> deletedUsers = service.fetchDeletedUsers(10);
        assertThat(deletedUsers).isEmpty();
        assertThat(service.hasMore()).isFalse();
        verify(lauClient, times(1)).getDeletedUsers(anyMap(), anyInt());
    }

    private DeletedUsersResponse makeDeletedUsersResponse() {
        return DeletedUsersResponse.builder()
            .startRecordNumber(10)
            .moreRecords(false)
            .deletionLogs(List.of(makeDeletionLog()))
            .build();
    }

    private DeletionLog makeDeletionLog() {
        return DeletionLog.builder()
            .userId("00001")
            .build();
    }

}
