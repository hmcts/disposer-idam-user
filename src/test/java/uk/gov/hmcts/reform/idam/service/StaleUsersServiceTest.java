package uk.gov.hmcts.reform.idam.service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.RestClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;


@ExtendWith(MockitoExtension.class)
class StaleUsersServiceTest {

    @Mock
    RestClient client;

    @Mock
    private ParameterResolver idamConfig;

    @InjectMocks
    private StaleUsersService staleUsersService;

    @BeforeEach
    void setUp() {
        when(idamConfig.getIdamHost()).thenReturn("");
        when(idamConfig.getStaleUsersPath()).thenReturn("/api/v2/staleUsers");
    }

    @Test
    void shouldMakeRequestOnFetchStaleUsers() {
        Response firstResponse = buildStaleUsersMockResponse(
            new StaleUsersResponse(List.of("1", "2"), true),
            OK.value()
        );

        when(client.getRequest(nullable(String.class), anyString(), anyMap(), anyMap()))
            .thenReturn(firstResponse);

        List<String> staleUsers = staleUsersService.fetchStaleUsers();
        assertThat(staleUsers).hasSize(2);
        verify(client, times(1)).getRequest(
            nullable(String.class), anyString(), anyMap(), anyMap());
    }

    @Test
    void shouldThrowExceptionOnBadResponseFromIdamApi() {
        Response response = buildStaleUsersMockResponse(
            null,
            INTERNAL_SERVER_ERROR.value()
        );
        when(client.getRequest(nullable(String.class), anyString(), anyMap(), anyMap()))
            .thenReturn(response);

        var thrown = assertThrows(
            IdamApiException.class,
            () -> ReflectionTestUtils.invokeMethod(staleUsersService, "fetchStaleUsers")
        );

        assertThat(thrown.getMessage())
            .contains("Stale users IDAM API call failed, status");

        verify(client, times(1)).getRequest(
            nullable(String.class), anyString(), anyMap(), anyMap());
    }


    private Response buildStaleUsersMockResponse(StaleUsersResponse entity, int respStatus) {
        Response resp = mock(Response.class);
        if (respStatus == 200) {
            when(resp.readEntity(StaleUsersResponse.class)).thenReturn(entity);
        }
        when(resp.getStatus()).thenReturn(respStatus);
        return resp;
    }


}
