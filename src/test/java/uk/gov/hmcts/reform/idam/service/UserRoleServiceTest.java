package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.service.remote.client.RoleAssignmentClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RequestedRole;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleAssignmentsMergeRequest;
import uk.gov.hmcts.reform.idam.service.remote.requests.RoleRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignment;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.idam.service.remote.responses.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.util.DuplicateUserSummary;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;


@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {
    @Mock
    RoleAssignmentClient roleAssignmentClient;

    @Mock
    SecurityUtil securityUtil;

    @Mock
    DuplicateUserSummary duplicateUserSummary;

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

    @Test
    void shouldMakeMergeRequest() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse();
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        Response createRoleAssignmentResponse = mock(Response.class);
        when(createRoleAssignmentResponse.status()).thenReturn(OK.value());
        when(roleAssignmentClient.createRoleAssignment(anyMap(), any())).thenReturn(createRoleAssignmentResponse);

        // -- make a call
        userRoleService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));

        var mergeRequestCaptor = ArgumentCaptor.forClass(RoleAssignmentsMergeRequest.class);
        verify(roleAssignmentClient, times(1)).createRoleAssignment(anyMap(), mergeRequestCaptor.capture());

        RoleRequest submittedRoleRequest = mergeRequestCaptor.getValue().getRoleRequest();
        assertThat(submittedRoleRequest.getReference()).isEqualTo("caseId_1-activeId");
        assertThat(submittedRoleRequest.isReplaceExisting()).isFalse();

        List<RequestedRole> submittedRequestedRoles = mergeRequestCaptor.getValue().getRequestedRoles();
        assertThat(submittedRequestedRoles).hasSize(1);
        assertThat(submittedRequestedRoles.get(0).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles.get(0).getAttributes().getCaseId()).isEqualTo("caseId_1");
    }

    @Test
    void shouldIncreaseFailedMergeCounterOnFailure() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse();
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        Response createRoleAssignmentResponse = mock(Response.class);
        when(createRoleAssignmentResponse.status()).thenReturn(BAD_REQUEST.value());
        when(roleAssignmentClient.createRoleAssignment(anyMap(), any())).thenReturn(createRoleAssignmentResponse);

        // -- make a call
        userRoleService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));

        var mergeRequestCaptor = ArgumentCaptor.forClass(RoleAssignmentsMergeRequest.class);
        verify(roleAssignmentClient, times(1)).createRoleAssignment(anyMap(), mergeRequestCaptor.capture());
        verify(duplicateUserSummary, times(1)).increaseFailedMerge();
    }

    private RoleAssignment makeRoleAssignment(String userId) {
        return RoleAssignment.builder()
            .actorIdType("IDAM")
            .actorId(userId)
            .build();
    }

    private RoleAssignmentResponse createRoleAssignmentResponse() {
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        RoleAssignmentAttributes attributes = RoleAssignmentAttributes.builder()
            .caseId("caseId_1")
            .build();
        RoleAssignment assignment = RoleAssignment.builder()
            .attributes(attributes)
            .build();
        roleAssignments.add(assignment);
        return new RoleAssignmentResponse(roleAssignments);

    }

}

