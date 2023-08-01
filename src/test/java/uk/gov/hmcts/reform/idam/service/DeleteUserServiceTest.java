package uk.gov.hmcts.reform.idam.service;


import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.service.remote.IdamClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    @Mock
    private IdamClient client;

    @Captor
    ArgumentCaptor<String> pathCaptor;

    @InjectMocks
    private DeleteUserService deleteUserService;

    @Test
    void shouldMakeDeleteRequest() {
        Response response = mock(Response.class);
        when(response.status()).thenReturn(NO_CONTENT.value());
        when(client.deleteUser(anyString())).thenReturn(response);
        List<String> staleUserIds = List.of("a", "b", "c");
        deleteUserService.deleteUsers(staleUserIds);
        verify(client, times(3)).deleteUser(pathCaptor.capture());
        assertThat(pathCaptor.getAllValues()).isEqualTo(staleUserIds);
    }
}
