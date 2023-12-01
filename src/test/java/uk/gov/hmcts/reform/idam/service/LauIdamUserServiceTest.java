package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods")
class LauIdamUserServiceTest {

    @Mock
    IdamTokenGenerator idamTokenGenerator;

    @Mock
    ServiceTokenGenerator serviceTokenGenerator;

    @Mock
    LauIdamClient lauClient;

    @InjectMocks
    LauIdamUserService service;

    @BeforeEach
    void setUp() {
        when(idamTokenGenerator.getPasswordTypeAuthorizationHeader()).thenReturn("Authorization: Bearer 123");
        when(serviceTokenGenerator.getServiceAuthToken()).thenReturn("Bearer Service");
    }

    @Test
    void fetchDeletedUsersShouldCallLauService() {
        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt())).thenReturn(makeDeletedUsersResponse());
        List<DeletionLog> deletedUsers = service.fetchDeletedUsers();
        assertThat(deletedUsers).hasSize(1);
        assertThat(service.hasMoreRecords()).isFalse();
        verify(lauClient, times(1)).getDeletedUsers(anyMap(), anyInt(), anyInt());
    }


    @Test
    void fetchDeletedUsersShouldReturnEmptyListOnException() {
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        byte[] body = {};

        when(lauClient.getDeletedUsers(anyMap(), anyInt(), anyInt()))
            .thenThrow(new FeignException.GatewayTimeout("Unauthorized", request, body, null));
        List<DeletionLog> deletedUsers = service.fetchDeletedUsers();
        assertThat(deletedUsers).isEmpty();
        assertThat(service.hasMoreRecords()).isFalse();
        verify(lauClient, times(1)).getDeletedUsers(anyMap(), anyInt(), anyInt());
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
