package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {
    @Mock
    RoleAssignmentClient roleAssignmentClient;

    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        var headers = Map.of(
            "Authorization", "Bearer 123456",
            "ServiceAuthorization", "Bearer 123456"
        );
        when(securityUtil.getAuthHeaders()).thenReturn(headers);
    }

    @Test
    void shouldFilterUsersWithRoles() {

        List<RoleAssignment> assignments = List.of(makeRoleAssignment("user-1"), makeRoleAssignment("user-2"));
        var entity = new RoleAssignmentResponse();
        entity.setRoleAssignments(assignments);

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(roleAssignmentClient.getRoleAssignments(anyMap(), any())).thenReturn(entity);

        List<String> users = userRoleService.filterUsersWithRoles(staleUsers);
        assertThat(users).hasSize(1);
        verify(roleAssignmentClient, times(1)).getRoleAssignments(anyMap(), any());
    }

    @Test
    void shouldReturnAllOnEmptyAssignments() {
        var roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(List.of());

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(roleAssignmentClient.getRoleAssignments(anyMap(), any())).thenReturn(roleAssignmentResponse);
        List<String> users = userRoleService.filterUsersWithRoles(staleUsers);
        assertThat(users).hasSize(3);
        verify(roleAssignmentClient, times(1)).getRoleAssignments(anyMap(), any());
    }


    private RoleAssignment makeRoleAssignment(String userId) {
        return RoleAssignment.builder()
            .actorIdType("IDAM")
            .actorId(userId)
            .build();
    }
}

