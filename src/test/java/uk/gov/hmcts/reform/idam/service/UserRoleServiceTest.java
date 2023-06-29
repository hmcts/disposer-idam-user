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
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;


@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {
    @Mock
    RestClient client;

    @Mock
    private ParameterResolver idamConfig;

    @InjectMocks
    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        when(idamConfig.getIdamHost()).thenReturn("");
        when(idamConfig.getRoleAssignmentsPath()).thenReturn("/am/role-assignments/query");
        when(idamConfig.getRoleAssignmentsContentType()).thenReturn("");
    }

    @Test
    void shouldFilterUsersWithRoles() {
        List<RoleAssignment> assignments = List.of(makeRoleAssignment("user-1"), makeRoleAssignment("user-2"));
        var entity = new RoleAssignmentResponse();
        entity.setRoleAssignments(assignments);
        Response response = buildRoleAssignmentMockResponse(entity, OK.value());

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(client.postRequest(nullable(String.class), anyString(), any(), any()))
            .thenReturn(response);
        List<String> users = userRoleService.filterUsersWithRoles(staleUsers);
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
        List<String> users = userRoleService.filterUsersWithRoles(staleUsers);
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

        var thrown = assertThrows(
            IdamApiException.class,
            () -> ReflectionTestUtils.invokeMethod(userRoleService, "filterUsersWithRoles", staleUsers)
        );

        assertThat(thrown.getMessage())
            .contains("User role assignments IDAM API call failed");

        verify(client, times(1)).postRequest(nullable(String.class), anyString(), any(), any());
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

