package uk.gov.hmcts.reform.idam.service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.RestClient;
import uk.gov.hmcts.reform.idam.service.remote.StaleUsersRequestParams;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class StaleUsersServiceTest {
    @Mock
    RestClient client;

    @Mock
    private StaleUsersRequestParams requestParams;

    @Mock
    private ParameterResolver idamConfig;

    @InjectMocks
    private StaleUsersService service;

    @BeforeEach
    void setUp() {
        when(idamConfig.getIdamHost()).thenReturn("");
    }

    @Test
    void shouldMakeARequestOnRetrieveStaleUsers() {

        Response firstResponse = buildStaleUsersMockResponse(
            new StaleUsersResponse(List.of("1", "2"), true),
            OK.value()
        );
        Response secondResponse = buildStaleUsersMockResponse(
            new StaleUsersResponse(List.of("3", "4"), false),
            OK.value()
        );

        when(client.getRequest(nullable(String.class), anyString(), anyMap(), anyMap()))
            .thenReturn(firstResponse)
            .thenReturn(secondResponse);

        List<String> staleUsers = service.retrieveStaleUsers();
        assertThat(staleUsers).hasSize(4);
        verify(client, times(2)).getRequest(
            nullable(String.class), anyString(), anyMap(), anyMap());
    }

    @Test
    void shouldStopRequestingStaleUsersOnBadResponse() {
        Response response = buildStaleUsersMockResponse(
            null,
            INTERNAL_SERVER_ERROR.value()
        );
        when(client.getRequest(nullable(String.class), anyString(), anyMap(), anyMap()))
            .thenReturn(response);

        List<String> staleUsers = service.retrieveStaleUsers();
        assertThat(staleUsers).isEmpty();
        verify(client, times(1)).getRequest(
            nullable(String.class), anyString(), anyMap(), anyMap());
    }

    @Test
    void shouldFilterUsersOut() {
        List<RoleAssignment> assignments = List.of(makeRoleAssignment("user-1"), makeRoleAssignment("user-2"));
        var entity = new RoleAssignmentResponse();
        entity.setRoleAssignments(assignments);
        Response response = buildRoleAssignmentMockResponse(entity, OK.value());

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(client.postRequest(nullable(String.class), anyString(), any(), any()))
            .thenReturn(response);
        List<String> users = service.filterUsersWithRoleAssignments(staleUsers);
        assertThat(users).hasSize(1);
        verify(client, times(1)).postRequest(nullable(String.class), anyString(), any(), any());
    }

    @Test
    void shouldReturnAllOnEmptyAssignments() {
        List<RoleAssignment> assignments = List.of();
        var entity = new RoleAssignmentResponse();
        entity.setRoleAssignments(assignments);
        Response response = buildRoleAssignmentMockResponse(entity, OK.value());

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(client.postRequest(nullable(String.class), anyString(), any(), any()))
            .thenReturn(response);
        List<String> users = service.filterUsersWithRoleAssignments(staleUsers);
        assertThat(users).hasSize(3);
        verify(client, times(1)).postRequest(nullable(String.class), anyString(), any(), any());

    }

    @Test
    void shouldReturnEmptyListOnUnsuccessfulFetch() {
        List<RoleAssignment> assignments = List.of(makeRoleAssignment("user-1"), makeRoleAssignment("user-2"));
        var entity = new RoleAssignmentResponse();
        entity.setRoleAssignments(assignments);
        Response response = buildRoleAssignmentMockResponse(entity, BAD_REQUEST.value());

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(client.postRequest(nullable(String.class), anyString(), any(), any()))
            .thenReturn(response);
        List<String> users = service.filterUsersWithRoleAssignments(staleUsers);
        assertThat(users).isEmpty();
        verify(client, times(1)).postRequest(nullable(String.class), anyString(), any(), any());
    }

    private Response buildStaleUsersMockResponse(StaleUsersResponse entity, int respStatus) {
        Response resp = mock(Response.class);
        if (respStatus == 200) {
            when(resp.readEntity(StaleUsersResponse.class)).thenReturn(entity);
        }
        when(resp.getStatus()).thenReturn(respStatus);
        return resp;
    }

    private Response buildRoleAssignmentMockResponse(RoleAssignmentResponse entity, int httpStatus) {
        Response resp = mock(Response.class);
        if (httpStatus < 300) {
            when(resp.readEntity(RoleAssignmentResponse.class)).thenReturn(entity);
        }
        when(resp.getStatus()).thenReturn(httpStatus);
        return resp;
    }

    private RoleAssignment makeRoleAssignment(String userId) {
        return RoleAssignment.builder()
            .actorIdType("IDAM")
            .actorId(userId)
            .build();
    }
}
