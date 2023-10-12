package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.UserContent;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class StaleUsersServiceTest {

    @Mock
    IdamClient idamClient;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private IdamTokenGenerator idamTokenGenerator;

    @InjectMocks
    private StaleUsersService staleUsersService;

    @BeforeEach
    void setUp() {
        when(parameterResolver.getBatchSize()).thenReturn(2);
        when(parameterResolver.getIdamRoleToDelete()).thenReturn("citizen");
    }

    @Test
    void shouldMakeRequestOnFetchStaleUsers() {
        List<UserContent> userContentList = new LinkedList<>();
        userContentList.add(new UserContent("1", List.of("citizen")));
        userContentList.add(new UserContent("2", List.of("defendant")));
        StaleUsersResponse response = new StaleUsersResponse(userContentList, false);

        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer token");
        when(idamClient.getStaleUsers(anyString(), any())).thenReturn(response);

        List<String> staleUsers = staleUsersService.fetchStaleUsers();
        assertThat(staleUsers).hasSize(1);
        verify(idamClient, times(1)).getStaleUsers(anyString(), any());
        assertThat(staleUsersService.hasFinished()).isFalse();
    }

    @Test
    void shouldFilterStaleUsersByCitizenAccount() {
        final List<UserContent> userContentList = new LinkedList<>();
        userContentList.add(new UserContent("1", Arrays.asList("citizen", "defendant")));
        userContentList.add(new UserContent("2", List.of("defendant")));
        userContentList.add(new UserContent("3", List.of("citizen")));
        userContentList.add(new UserContent("4", List.of("CitIZen")));
        userContentList.add(new UserContent("5", Arrays.asList("CitIZen", "judge")));
        userContentList.add(new UserContent("6", null));
        userContentList.add(new UserContent("7", emptyList()));

        final StaleUsersResponse response = new StaleUsersResponse(userContentList, false);

        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer token");
        when(idamClient.getStaleUsers(anyString(), any())).thenReturn(response);

        final List<String> staleUsers = staleUsersService.fetchStaleUsers();
        assertThat(staleUsers).hasSize(2);
        assertThat(staleUsers.get(0)).isEqualTo("3");
        assertThat(staleUsers.get(1)).isEqualTo("4");
    }

}
