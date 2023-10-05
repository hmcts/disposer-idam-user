package uk.gov.hmcts.reform.idam.service;


import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerErrorException;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    private static final String HEADER = "Authorization: Bearer 123456";
    @Mock
    private IdamClient idamClient;

    @Mock
    IdamTokenGenerator idamTokenGenerator;

    @Mock
    ParameterResolver parameterResolver;

    @Captor
    ArgumentCaptor<String> pathCaptor;

    @InjectMocks
    private DeleteUserService deleteUserService;

    @Test
    void shouldMakeDeleteRequest() {
        Response response = mock(Response.class);

        when(response.status()).thenReturn(OK.value());
        when(idamClient.deleteUser(anyString(), anyString())).thenReturn(response);
        when(parameterResolver.getIsSimulation()).thenReturn(false);
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn(HEADER);

        List<String> staleUserIds = List.of("a", "b", "c");
        deleteUserService.deleteUsers(staleUserIds);
        verify(idamClient, times(3)).deleteUser(anyString(), pathCaptor.capture());
        assertThat(pathCaptor.getAllValues()).isEqualTo(staleUserIds);
    }

    @Test
    void shouldCatchAndRethrowExceptionOnIdamClientError() {
        when(idamClient.deleteUser(HEADER, "userId"))
            .thenThrow(new ServerErrorException("Internal Server Error", null));
        when(parameterResolver.getIsSimulation()).thenReturn(false);
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn(HEADER);

        List<String> staleUserIds = List.of("userId");

        assertThrows(ServerErrorException.class, () -> deleteUserService.deleteUsers(staleUserIds));
        verify(idamClient, times(1)).deleteUser(HEADER, "userId");
    }

    @Test
    void shouldThrowIdamApiExceptionOnNon200Response() {
        Response response = mock(Response.class);

        when(response.status()).thenReturn(BAD_REQUEST.value());
        when(idamClient.deleteUser(anyString(), anyString())).thenReturn(response);
        when(parameterResolver.getIsSimulation()).thenReturn(false);
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn(HEADER);

        List<String> staleUserIds = List.of("userId");
        Exception thrown = assertThrows(IdamApiException.class, () -> deleteUserService.deleteUsers(staleUserIds));
        assertThat(thrown.getMessage()).isEqualTo("User with id 'userId' deletion failed (response status 400)");
    }

    @Test
    void shouldNotMakeDeleteRequest() {
        when(parameterResolver.getIsSimulation()).thenReturn(true);

        List<String> staleUserIds = List.of("a", "b", "c");
        deleteUserService.deleteUsers(staleUserIds);
        verify(idamClient, times(0)).deleteUser(anyString(),  anyString());
    }
}
