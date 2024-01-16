package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import static org.springframework.http.HttpStatus.CREATED;


@ExtendWith(MockitoExtension.class)
class UserRoleMergeServiceTest {

    @Mock
    RoleAssignmentClient roleAssignmentClient;

    @Mock
    DuplicateUserSummary duplicateUserSummary;

    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    UserRoleMergeService userRoleMergeService;

    @BeforeEach
    void setUp() {
        var headers = Map.of(
            "Authorization", "Bearer 123456",
            "ServiceAuthorization", "Bearer 123456"
        );
        when(securityUtil.getAuthHeaders()).thenReturn(headers);
    }

    @Test
    void shouldMakeMergeRequest() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse(List.of("caseId_1"));
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        Response createRoleAssignmentResponse = mock(Response.class);
        when(createRoleAssignmentResponse.status()).thenReturn(CREATED.value());
        when(roleAssignmentClient.createRoleAssignment(anyMap(), any())).thenReturn(createRoleAssignmentResponse);

        // -- make a call
        userRoleMergeService.mergeRoleAssignments("archivedId", "activeId");

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
        verify(duplicateUserSummary, times(0)).increaseFailedMerge();
        verify(duplicateUserSummary, times(1)).increaseMerged();
    }

    @Test
    void shouldMakeMergeRequestWithMultipleRoles() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse(List.of("caseId_1", "caseId_1", "caseId_1"));
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        Response createRoleAssignmentResponse = mock(Response.class);
        when(createRoleAssignmentResponse.status()).thenReturn(CREATED.value());
        when(roleAssignmentClient.createRoleAssignment(anyMap(), any())).thenReturn(createRoleAssignmentResponse);

        // -- make a call
        userRoleMergeService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));

        var mergeRequestCaptor = ArgumentCaptor.forClass(RoleAssignmentsMergeRequest.class);
        verify(roleAssignmentClient, times(1)).createRoleAssignment(anyMap(), mergeRequestCaptor.capture());

        RoleRequest submittedRoleRequest = mergeRequestCaptor.getValue().getRoleRequest();
        assertThat(submittedRoleRequest.getReference()).isEqualTo("caseId_1-activeId");
        assertThat(submittedRoleRequest.isReplaceExisting()).isFalse();

        List<RequestedRole> submittedRequestedRoles = mergeRequestCaptor.getValue().getRequestedRoles();
        assertThat(submittedRequestedRoles).hasSize(3);
        assertThat(submittedRequestedRoles.get(0).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles.get(0).getAttributes().getCaseId()).isEqualTo("caseId_1");
        assertThat(submittedRequestedRoles.get(1).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles.get(1).getAttributes().getCaseId()).isEqualTo("caseId_1");
        assertThat(submittedRequestedRoles.get(2).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles.get(2).getAttributes().getCaseId()).isEqualTo("caseId_1");
        verify(duplicateUserSummary, times(0)).increaseFailedMerge();
        verify(duplicateUserSummary, times(1)).increaseMerged();
    }

    @Test
    void shouldMakeMultipleMergeRequestsWithMultipleCaseIds() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse(List.of("caseId_0", "caseId_1", "caseId_2"));
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        Response createRoleAssignmentResponse = mock(Response.class);
        when(createRoleAssignmentResponse.status()).thenReturn(CREATED.value());
        when(roleAssignmentClient.createRoleAssignment(anyMap(), any())).thenReturn(createRoleAssignmentResponse);

        // -- make a call
        userRoleMergeService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));

        var mergeRequestCaptor = ArgumentCaptor.forClass(RoleAssignmentsMergeRequest.class);
        verify(roleAssignmentClient, times(3)).createRoleAssignment(anyMap(), mergeRequestCaptor.capture());

        RoleRequest submittedRoleRequest0 = mergeRequestCaptor.getAllValues().get(0).getRoleRequest();
        assertThat(submittedRoleRequest0.getReference()).isEqualTo("caseId_0-activeId");
        assertThat(submittedRoleRequest0.isReplaceExisting()).isFalse();

        RoleRequest submittedRoleRequest1 = mergeRequestCaptor.getAllValues().get(1).getRoleRequest();
        assertThat(submittedRoleRequest1.getReference()).isEqualTo("caseId_1-activeId");
        assertThat(submittedRoleRequest1.isReplaceExisting()).isFalse();

        RoleRequest submittedRoleRequest2 = mergeRequestCaptor.getAllValues().get(2).getRoleRequest();
        assertThat(submittedRoleRequest2.getReference()).isEqualTo("caseId_2-activeId");
        assertThat(submittedRoleRequest2.isReplaceExisting()).isFalse();

        List<RequestedRole> submittedRequestedRoles0 = mergeRequestCaptor.getAllValues().get(0).getRequestedRoles();
        assertThat(submittedRequestedRoles0).hasSize(1);
        assertThat(submittedRequestedRoles0.get(0).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles0.get(0).getAttributes().getCaseId()).isEqualTo("caseId_0");

        List<RequestedRole> submittedRequestedRoles1 = mergeRequestCaptor.getAllValues().get(1).getRequestedRoles();
        assertThat(submittedRequestedRoles1).hasSize(1);
        assertThat(submittedRequestedRoles1.get(0).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles1.get(0).getAttributes().getCaseId()).isEqualTo("caseId_1");

        List<RequestedRole> submittedRequestedRoles2 = mergeRequestCaptor.getAllValues().get(2).getRequestedRoles();
        assertThat(submittedRequestedRoles2).hasSize(1);
        assertThat(submittedRequestedRoles2.get(0).getActorId()).isEqualTo("activeId");
        assertThat(submittedRequestedRoles2.get(0).getAttributes().getCaseId()).isEqualTo("caseId_2");

        verify(duplicateUserSummary, times(0)).increaseFailedMerge();
        verify(duplicateUserSummary, times(3)).increaseMerged();
    }


    @Test
    void shouldNotMakeMergeRequestsIfDryRunMode() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse(List.of("caseId_1"));
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        ReflectionTestUtils.setField(userRoleMergeService, "dryRun", true);

        // -- make a call
        userRoleMergeService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));
        verify(roleAssignmentClient, times(0)).createRoleAssignment(anyMap(), any(RoleAssignmentsMergeRequest.class));
    }

    @Test
    void shouldIncreaseFailedMergeCounterOnFailure() {
        // -- prepare
        RoleAssignmentResponse response = createRoleAssignmentResponse(List.of("caseId_1"));
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);
        Response createRoleAssignmentResponse = mock(Response.class);
        when(createRoleAssignmentResponse.status()).thenReturn(BAD_REQUEST.value());
        when(roleAssignmentClient.createRoleAssignment(anyMap(), any())).thenReturn(createRoleAssignmentResponse);

        // -- make a call
        userRoleMergeService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));

        var mergeRequestCaptor = ArgumentCaptor.forClass(RoleAssignmentsMergeRequest.class);
        verify(roleAssignmentClient, times(1)).createRoleAssignment(anyMap(), mergeRequestCaptor.capture());
        verify(duplicateUserSummary, times(1)).increaseFailedMerge();
    }

    @Test
    void shouldNotMergeIfArchivedUserHasNoRoleAssignments() {
        // -- prepare
        RoleAssignmentResponse response = new RoleAssignmentResponse(List.of());
        when(roleAssignmentClient.getRoleAssignmentsByUserId(anyMap(), eq("archivedId"))).thenReturn(response);

        // -- make a call
        userRoleMergeService.mergeRoleAssignments("archivedId", "activeId");

        // -- check the outcome
        verify(roleAssignmentClient, times(1)).getRoleAssignmentsByUserId(anyMap(), eq("archivedId"));
        verify(roleAssignmentClient, times(0)).createRoleAssignment(anyMap(), any(RoleAssignmentsMergeRequest.class));

        verify(duplicateUserSummary, times(0)).increaseFailedMerge();
        verify(duplicateUserSummary, times(0)).increaseMerged();
        verify(duplicateUserSummary, times(1)).increaseNoRoleAssignmentsOnUser();
    }

    private RoleAssignmentResponse createRoleAssignmentResponse(List<String> caseIds) {
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        for (String caseId: caseIds) {
            RoleAssignmentAttributes attributes = RoleAssignmentAttributes.builder()
                .caseId(caseId)
                .build();
            RoleAssignment assignment = RoleAssignment.builder()
                .attributes(attributes)
                .build();
            roleAssignments.add(assignment);
        }
        return new RoleAssignmentResponse(roleAssignments);
    }

}
