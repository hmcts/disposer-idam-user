package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.service.remote.responses.IdamQueryResponse;
import uk.gov.hmcts.reform.idam.util.DuplicateUserSummary;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class IdamDuplicateUserMergerServiceTest {

    @Mock
    LauIdamUserService lauService;
    @Mock
    DuplicateUserSummary duplicateUserSummary;
    @Mock
    IdamTokenGenerator idamTokenGenerator;

    @Mock
    IdamClient idamClient;

    @Mock
    UserRoleService userRoleService;

    @Mock
    LoggingSummaryUtils summaryUtils;

    @Captor
    ArgumentCaptor<Map<String, String>> queryParamCaptor;

    @InjectMocks
    IdamDuplicateUserMergerService duplicateUserMergerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(duplicateUserMergerService, "requestsLimit", 10);
        ReflectionTestUtils.setField(duplicateUserMergerService, "batchSize", 1);
        ReflectionTestUtils.setField(duplicateUserMergerService, "startPage", 1);
    }

    @Test
    void runShouldCallRetrieveDeletedUsers() {
        duplicateUserMergerService.run();
        verify(lauService, times(1)).retrieveDeletedUsers(duplicateUserMergerService, 10, 1, 1);
        verify(duplicateUserSummary, times(1)).setStartTime();
        verify(duplicateUserSummary, times(1)).setEndTime();
    }

    @Test
    void consumeLauDeletedUsersShouldIncreaseNoMatch() {
        List<DeletionLog> logs = List.of(makeDeletionLog("001", "email"));
        List<IdamQueryResponse> idamQueryResponses = List.of();
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("header");
        when(idamClient.queryUser(anyString(), anyMap())).thenReturn(idamQueryResponses);

        duplicateUserMergerService.consumeLauDeletedUsers(logs);

        verify(idamClient, times(1)).queryUser(anyString(), queryParamCaptor.capture());
        assertThat(queryParamCaptor.getValue()).containsEntry("query", "email:" + logs.get(0).getEmailAddress());
        verify(duplicateUserSummary, times(1)).increaseNoMatches();
    }

    @Test
    void consumeLauDeletedUsersShouldIncreaseMultipleMatches() {
        List<DeletionLog> logs = List.of(makeDeletionLog("001", "email"));
        List<IdamQueryResponse> idamQueryResponses = List.of(
            makeIdamQueryResponse("002", "email"),
            makeIdamQueryResponse("003", "email")
        );
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("header");
        when(idamClient.queryUser(anyString(), anyMap())).thenReturn(idamQueryResponses);

        duplicateUserMergerService.consumeLauDeletedUsers(logs);

        verify(idamClient, times(1)).queryUser(anyString(), queryParamCaptor.capture());
        assertThat(queryParamCaptor.getValue()).containsEntry("query", "email:" + logs.get(0).getEmailAddress());
        verify(duplicateUserSummary, times(1)).increaseMultipleMatches();
    }

    @Test
    void consumeLauDeletedUsersShouldIncreaseMatchedIds() {
        List<DeletionLog> logs = List.of(makeDeletionLog("001", "email"));
        List<IdamQueryResponse> idamQueryResponses = List.of(makeIdamQueryResponse("001", "email"));
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("header");
        when(idamClient.queryUser(anyString(), anyMap())).thenReturn(idamQueryResponses);

        duplicateUserMergerService.consumeLauDeletedUsers(logs);

        verify(idamClient, times(1)).queryUser(anyString(), queryParamCaptor.capture());
        assertThat(queryParamCaptor.getValue()).containsEntry("query", "email:" + logs.get(0).getEmailAddress());
        verify(duplicateUserSummary, times(1)).increaseMatchedIds();
    }

    @Test
    void consumeLauDeletedUsersShouldIncreaseMismatchedIds() {
        List<DeletionLog> logs = List.of(makeDeletionLog("001", "email"));
        List<IdamQueryResponse> idamQueryResponses = List.of(makeIdamQueryResponse("002", "email"));
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("header");
        when(idamClient.queryUser(anyString(), anyMap())).thenReturn(idamQueryResponses);

        duplicateUserMergerService.consumeLauDeletedUsers(logs);

        verify(idamClient, times(1)).queryUser(anyString(), queryParamCaptor.capture());
        assertThat(queryParamCaptor.getValue()).containsEntry("query", "email:" + logs.get(0).getEmailAddress());
        verify(duplicateUserSummary, times(1)).increaseEmailMultipleIds();
    }

    @Test
    void consumeLauDeletedUsersShouldCallIdamForEachDeletionLog() {
        List<DeletionLog> logs = List.of(
            makeDeletionLog("001", "email1"),
            makeDeletionLog("002", "email2"),
            makeDeletionLog("003", "email3")
        );
        List<IdamQueryResponse> response = List.of(makeIdamQueryResponse("002", "email"));
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("header");
        when(idamClient.queryUser(anyString(), anyMap())).thenReturn(response);

        duplicateUserMergerService.consumeLauDeletedUsers(logs);

        verify(idamClient, times(3)).queryUser(anyString(), queryParamCaptor.capture());
        assertThat(queryParamCaptor.getAllValues().get(0))
            .containsEntry("query", "email:" + logs.get(0).getEmailAddress());
        assertThat(queryParamCaptor.getAllValues().get(1))
            .containsEntry("query", "email:" + logs.get(1).getEmailAddress());
        assertThat(queryParamCaptor.getAllValues().get(2))
            .containsEntry("query", "email:" + logs.get(2).getEmailAddress());
    }

    DeletionLog makeDeletionLog(String userId, String email) {
        return DeletionLog.builder()
            .userId(userId)
            .emailAddress(email)
            .build();
    }

    IdamQueryResponse makeIdamQueryResponse(String userId, String email) {
        IdamQueryResponse queryResponse = new IdamQueryResponse();
        queryResponse.setId(userId);
        queryResponse.setEmail(email);
        return queryResponse;
    }
}
