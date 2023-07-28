package uk.gov.hmcts.reform.idam.service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    private static final String HOST = "http://localhost:5000";
    private static final String PATH = "/api/v2/users/";

    private static final int ATTEMPTS_TO_DELETE = 3;
    @Mock
    private RestClient client;

    @Mock
    private ParameterResolver idamConfig;

    @Captor
    ArgumentCaptor<String> pathCaptor;

    @InjectMocks
    private DeleteUserService deleteUserService;

    @BeforeEach
    void setUp() {
        when(idamConfig.getIdamHost()).thenReturn(HOST);
        when(idamConfig.getDeleteUserPath()).thenReturn(PATH);
    }

    @Test
    void shouldMakeDeleteRequest() {
        Response response = Response.noContent().build();
        when(client.deleteRequest(eq(HOST), anyString())).thenReturn(response);
        List<String> staleUserIds = List.of("a", "b", "c");
        deleteUserService.deleteUsers(staleUserIds);
        List<String> expected = staleUserIds.stream().map(id -> "/api/v2/users/" + id).toList();
        verify(client, times(3)).deleteRequest(eq(HOST), pathCaptor.capture());
        assertThat(pathCaptor.getAllValues()).isEqualTo(expected);
    }
}
