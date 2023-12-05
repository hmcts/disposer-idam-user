package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RestoreUserRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

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

    @Spy
    RestoreSummary restoreSummary;

    @Captor
    ArgumentCaptor<RestoreUserRequest> requestCaptor;

    @InjectMocks
    private RestoreUserService restoreUserService;

    @BeforeEach
    void setUp() {
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer 01");
    }

    @Test
    void restoreUserShouldCallIdamClientToRestoreUser() throws IOException {
        String userId = "00001";
        final DeletionLog deletionLog = createDeletionLog(userId);
        when(response.status()).thenReturn(HttpStatus.CREATED.value());
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);
        mockBodyWithMessage("hello");

        restoreUserService.restoreUser(deletionLog);

        verify(idamClient, times(1)).restoreUser(anyString(), eq(userId), requestCaptor.capture());

        RestoreUserRequest request = requestCaptor.getValue();

        assertThat(request.getId()).isEqualTo("00001");
        assertThat(request.getEmail()).isEqualTo(deletionLog.getEmailAddress());
        assertThat(request.getForename()).isEqualTo(deletionLog.getFirstName());
        assertThat(request.getSurname()).isEqualTo(deletionLog.getLastName());
        assertThat(request.getRoles()).hasSize(1);
        assertThat(request.getRoles().get(0)).isEqualTo("citizen");

        assertThat(restoreSummary.getSuccessful().size()).isEqualTo(1);
    }

    @Test
    void restoreUserReturns409ErrorWhenUserRestoredAndActive() throws IOException {
        final DeletionLog deletionLog = createDeletionLog("0001");
        when(response.status()).thenReturn(HttpStatus.CONFLICT.value());

        mockBodyWithMessage("id in use");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        restoreUserService.restoreUser(deletionLog);

        verify(idamClient, times(1)).restoreUser(anyString(), eq("0001"), requestCaptor.capture());
        assertThat(restoreSummary.getFailedToRestoreDueToReinstatedAndActiveAccount().size()).isEqualTo(1);
    }

    @Test
    void restoreUserReturns409ErrorWhenUserRestored() throws IOException {
        final DeletionLog deletionLog = createDeletionLog("0001");
        when(response.status()).thenReturn(HttpStatus.CONFLICT.value());

        mockBodyWithMessage("id already archived");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        restoreUserService.restoreUser(deletionLog);

        verify(idamClient, times(1)).restoreUser(anyString(), eq("0001"), requestCaptor.capture());
        assertThat(restoreSummary.getFailedToRestoreDueToReinstatedAccount().size()).isEqualTo(1);
    }

    @Test
    void restoreUserReturns409ErrorWhenUserIsAlreadyArchived() throws IOException {
        final DeletionLog deletionLog = createDeletionLog("0001");
        when(response.status()).thenReturn(HttpStatus.CONFLICT.value());

        mockBodyWithMessage("email already archived");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        restoreUserService.restoreUser(deletionLog);

        verify(idamClient, times(1)).restoreUser(anyString(), eq("0001"), requestCaptor.capture());
        assertThat(restoreSummary.getFailedToRestoreDueToDuplicateEmail().size()).isEqualTo(1);
    }

    @Test
    void restoreUserReturns409ErrorWhenUserHasDuplicateEmail() throws IOException {
        final DeletionLog deletionLog = createDeletionLog("0001");
        when(response.status()).thenReturn(HttpStatus.CONFLICT.value());

        mockBodyWithMessage("email already archived");
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        restoreUserService.restoreUser(deletionLog);

        verify(idamClient, times(1)).restoreUser(anyString(), eq("0001"), requestCaptor.capture());
        assertThat(restoreSummary.getFailedToRestoreDueToDuplicateEmail().size()).isEqualTo(1);
    }

    @Test
    void restoreUserReturnsErrorShouldNotLogSuccess() throws IOException {
        final DeletionLog deletionLog = createDeletionLog("0001");
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer 01");
        when(response.status()).thenReturn(HttpStatus.BAD_REQUEST.value());
        when(idamClient.restoreUser(anyString(), anyString(), any())).thenReturn(response);

        mockBodyWithMessage("hello");

        restoreUserService.restoreUser(deletionLog);

        verify(idamClient, times(1)).restoreUser(anyString(), eq("0001"), requestCaptor.capture());

        assertThat(restoreSummary.getFailed().size()).isEqualTo(1);
        assertThat(restoreSummary.getSuccessful()).isEmpty();
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
