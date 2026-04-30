package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.config.CcdProperties;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {
    @Mock
    RoleAssignmentClient roleAssignmentClient;

    @Mock
    SecurityUtil securityUtil;

    CcdProperties ccdProperties;

    @Captor
    ArgumentCaptor<Map<String, String>> roleAssignentsHeaderCaptor;

    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        Map<String, String> headers = Map.of(
            "Authorization", "Bearer 123456",
            "ServiceAuthorization", "Bearer 123456"
        );
        when(securityUtil.getAuthHeaders()).thenReturn(headers);
        ccdProperties = new CcdProperties();
        CcdProperties.RoleAssignment roleAssignmentProperties = new CcdProperties.RoleAssignment();
        roleAssignmentProperties.setBatchSize(2);
        roleAssignmentProperties.setRequestPageSize(1000);
        ccdProperties.setRoleAssignment(roleAssignmentProperties);
        userRoleService = new UserRoleService(roleAssignmentClient, securityUtil, ccdProperties);
    }

    @Test
    void shouldFilterUsersWithRoles() {
        List<RoleAssignment> assignments = List.of(makeRoleAssignment("user-1"), makeRoleAssignment("user-2"));
        RoleAssignmentResponse entity = new RoleAssignmentResponse();
        entity.setRoleAssignments(assignments);

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");
        when(roleAssignmentClient.getRoleAssignments(anyMap(), any())).thenReturn(entity);

        List<String> users = userRoleService.filterUsersWithRoles(staleUsers);
        assertThat(users).hasSize(1);
        verify(roleAssignmentClient, times(1)).getRoleAssignments(anyMap(), any());
    }

    @Test
    void shouldReturnAllOnEmptyAssignments() {
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse(new ArrayList<>());

        List<String> staleUsers = List.of("user-1", "user-2", "user-3");

        when(roleAssignmentClient.getRoleAssignments(anyMap(), any())).thenReturn(roleAssignmentResponse);

        List<String> users = userRoleService.filterUsersWithRoles(staleUsers);
        assertThat(users).hasSize(3);
        verify(roleAssignmentClient, times(1)).getRoleAssignments(anyMap(), any());
    }

    @Test
    void shouldGiveAppropriateQuerySize() {
        final List<String> staleUsers = List.of("user-1", "user-2", "user-3");

        RoleAssignmentResponse roleAssignmentResponse1 = new RoleAssignmentResponse(getRoleAssignmentList(20));
        RoleAssignmentResponse roleAssignmentResponse2 = new RoleAssignmentResponse(getRoleAssignmentList(10));

        when(roleAssignmentClient.getRoleAssignments(anyMap(), any()))
            .thenReturn(roleAssignmentResponse1)
            .thenReturn(roleAssignmentResponse2);

        userRoleService.filterUsersWithRoles(staleUsers);

        verify(roleAssignmentClient, times(2))
            .getRoleAssignments(roleAssignentsHeaderCaptor.capture(), any());

        final List<Map<String, String>> allValues = roleAssignentsHeaderCaptor.getAllValues();

        assertThat(allValues.getFirst().get("size")).isEqualTo("20");
        assertThat(allValues.getLast().get("size")).isEqualTo("20");
    }

    @Test
    void shouldLoadMultipleRoleAssignmentsPages() {
        final List<String> staleUsers = List.of("user-1", "user-2", "user-3");

        RoleAssignmentResponse roleAssignmentResponse1 = new RoleAssignmentResponse(getRoleAssignmentList(20));
        RoleAssignmentResponse roleAssignmentResponse2 = new RoleAssignmentResponse(getRoleAssignmentList(10));

        when(roleAssignmentClient.getRoleAssignments(anyMap(), any()))
            // The first RA request wll return 20 roles -->  20 == (requestLimit x10)
            .thenReturn(roleAssignmentResponse1)
            // No more requests will be made because --> 10 != (requestLimit x10)
            .thenReturn(roleAssignmentResponse2);

        userRoleService.filterUsersWithRoles(staleUsers);

        verify(roleAssignmentClient, times(2))
            .getRoleAssignments(roleAssignentsHeaderCaptor.capture(), any());

        final List<Map<String, String>> allValues = roleAssignentsHeaderCaptor.getAllValues();

        assertThat(allValues.getFirst()).containsEntry("pageNumber", "0");
        assertThat(allValues.getLast()).containsEntry("pageNumber", "1");
    }

    private List<RoleAssignment> getRoleAssignmentList(final int size) {
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            roleAssignmentList.add(mock(RoleAssignment.class));
        }
        return roleAssignmentList;
    }

    private RoleAssignment makeRoleAssignment(String userId) {
        return RoleAssignment.builder()
            .actorIdType("IDAM")
            .actorId(userId)
            .build();
    }
}
