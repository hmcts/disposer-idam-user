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

import java.util.LinkedList;
import java.util.List;

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
    private ParameterResolver idamConfig;

    @Mock
    private IdamTokenGenerator idamTokenGenerator;

    @InjectMocks
    private StaleUsersService staleUsersService;

    @BeforeEach
    void setUp() {
        when(idamConfig.getBatchSize()).thenReturn(2);
    }

    @Test
    void shouldMakeRequestOnFetchStaleUsers() {
        List<UserContent> userContentList = new LinkedList<>();
        userContentList.add(new UserContent("1"));
        userContentList.add(new UserContent("2"));
        StaleUsersResponse response = new StaleUsersResponse(userContentList, false);

        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer token");
        when(idamClient.getStaleUsers(anyString(), any())).thenReturn(response);

        List<String> staleUsers = staleUsersService.fetchStaleUsers();
        assertThat(staleUsers).hasSize(2);
        verify(idamClient, times(1)).getStaleUsers(anyString(), any());
        assertThat(staleUsersService.hasFinished()).isFalse();
    }

}
