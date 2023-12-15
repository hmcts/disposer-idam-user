package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.remote.client.LauIdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods")
class LauIdamUserServiceTest {


    @Mock
    SecurityUtil securityUtil;

    @Mock
    LauIdamClient lauClient;

    @Captor
    ArgumentCaptor<List<DeletionLog>> deletionLogsCaptor;

    @InjectMocks
    LauIdamUserService service;

    @BeforeEach
    void setUp() {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer client",
            "ServiceAuthorization", "Bearer service"
        );
        when(securityUtil.getAuthHeaders()).thenReturn(headers);
    }

    @Test
    void retrieveDeletedUsersShouldInvokeLauIdamClient() {
        var response = makeDeletedUsersResponse("0001", false);
        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt())).thenReturn(response);
        LauDeletedUsersConsumer consumer = mock(LauDeletedUsersConsumer.class);

        service.retrieveDeletedUsers(consumer, 1, 1, 1);

        verify(lauClient, times(1)).getDeletedUsers(anyMap(), eq(1), eq(1));
        verify(consumer, times(1)).consumeLauDeletedUsers(deletionLogsCaptor.capture());
        assertThat(deletionLogsCaptor.getValue().get(0).getUserId())
            .isEqualTo(response.getDeletionLogs().get(0).getUserId());
    }

    @Test
    void retrieveDeletedUsersShouldCallWithProvidedPage() {
        var response = makeDeletedUsersResponse("0001", false);
        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt())).thenReturn(response);
        LauDeletedUsersConsumer consumer = mock(LauDeletedUsersConsumer.class);

        service.retrieveDeletedUsers(consumer, 1, 1, 10);

        verify(lauClient, times(1)).getDeletedUsers(anyMap(), eq(1), eq(10));
        verify(consumer, times(1)).consumeLauDeletedUsers(deletionLogsCaptor.capture());
        assertThat(deletionLogsCaptor.getValue().get(0).getUserId())
            .isEqualTo(response.getDeletionLogs().get(0).getUserId());
    }

    @Test
    void retrieveDeletedUsersShouldCallWhileMoreRecordsAndRequestsLimitNotExceeded() {
        LauDeletedUsersConsumer consumer = mock(LauDeletedUsersConsumer.class);
        var responseMoreRecords = makeDeletedUsersResponse("0001", true);
        var responseNoMoreRecords = makeDeletedUsersResponse("0002", false);
        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt()))
            .thenReturn(responseMoreRecords)
            .thenReturn(responseNoMoreRecords);

        service.retrieveDeletedUsers(consumer, 5, 10, 1);

        verify(lauClient, times(1)).getDeletedUsers(anyMap(), eq(10), eq(1));
        verify(lauClient, times(1)).getDeletedUsers(anyMap(), eq(10), eq(2));
    }

    @Test
    void retrieveDeletedUsersShouldCallUntilRequestLimitExceeds() {
        LauDeletedUsersConsumer consumer = mock(LauDeletedUsersConsumer.class);
        var responseMoreRecords = makeDeletedUsersResponse("0001", true);
        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt())).thenReturn(responseMoreRecords);

        service.retrieveDeletedUsers(consumer, 5, 10, 1);

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(lauClient, times(5)).getDeletedUsers(anyMap(), eq(10), captor.capture());
        verify(consumer, times(5)).consumeLauDeletedUsers(deletionLogsCaptor.capture());
        assertThat(captor.getAllValues()).isEqualTo(List.of(1, 2, 3, 4, 5));
    }

    @Test
    void retrieveDeletedUsersShouldRetryOnSpecificExceptions() {
        var responseMoreRecords = makeDeletedUsersResponse("0001", true);
        var responseNoMoreRecords = makeDeletedUsersResponse("0002", false);
        LauDeletedUsersConsumer consumer = mock(LauDeletedUsersConsumer.class);
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        byte[] body = {};

        ReflectionTestUtils.setField(service, "maxBackoffInSecondsThenGiveUp", 2L);

        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt()))
            .thenThrow(new FeignException.GatewayTimeout("GATEWAY TIMEOUT", request, body, null))
            .thenReturn(responseMoreRecords)
            .thenThrow(new FeignException.BadGateway("BAD GATEWAY", request, body, null))
            .thenReturn(responseMoreRecords)
            .thenThrow(new FeignException.Forbidden("FORBIDDEN", request, body, null))
            .thenReturn(responseNoMoreRecords);

        service.retrieveDeletedUsers(consumer, 3, 1, 1);

        ArgumentCaptor<Integer> pageParam = ArgumentCaptor.forClass(Integer.class);
        verify(securityUtil, times(1)).generateTokens();
        verify(lauClient, times(6)).getDeletedUsers(anyMap(), anyInt(), pageParam.capture());
        assertThat(pageParam.getAllValues()).isEqualTo(List.of(1, 1, 2, 2, 3, 3));
    }

    @Test
    void retrieveDeletedUsersShouldNotRetryOnNonListedExceptions() {
        var responseMoreRecords = makeDeletedUsersResponse("0001", true);
        LauDeletedUsersConsumer consumer = mock(LauDeletedUsersConsumer.class);
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        byte[] body = {};

        ReflectionTestUtils.setField(service, "maxBackoffInSecondsThenGiveUp", 2L);

        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt()))
            .thenThrow(new FeignException.BadRequest("BAD REQUEST", request, body, null))
            .thenReturn(responseMoreRecords);

        service.retrieveDeletedUsers(consumer, 3, 1, 1);

        ArgumentCaptor<Integer> pageParam = ArgumentCaptor.forClass(Integer.class);
        verify(securityUtil, times(1)).getAuthHeaders();
        verify(lauClient, times(1)).getDeletedUsers(anyMap(), anyInt(), pageParam.capture());
        assertThat(pageParam.getAllValues()).isEqualTo(List.of(1));
    }

    private DeletedUsersResponse makeDeletedUsersResponse(String userId, boolean moreRecords) {
        return DeletedUsersResponse.builder()
            .startRecordNumber(10)
            .moreRecords(moreRecords)
            .deletionLogs(List.of(makeDeletionLog(userId)))
            .build();
    }

    private DeletionLog makeDeletionLog(String userId) {
        return DeletionLog.builder()
            .userId(userId)
            .build();
    }

}
