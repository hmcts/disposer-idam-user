package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class StaleUsersServiceTest {

    @Mock
    IdamClient client;

    @Mock
    private ParameterResolver idamConfig;

    @InjectMocks
    private StaleUsersService staleUsersService;

    @BeforeEach
    void setUp() {
        when(idamConfig.getBatchSize()).thenReturn(2);
    }

    @Test
    void shouldMakeRequestOnFetchStaleUsers() {
        StaleUsersResponse response = new StaleUsersResponse(List.of("1", "2"), true);

        when(client.getStaleUsers(any())).thenReturn(response);

        List<String> staleUsers = staleUsersService.fetchStaleUsers();
        assertThat(staleUsers).hasSize(2);
        verify(client, times(1)).getStaleUsers(any());
        assertThat(staleUsersService.hasFinished()).isFalse();
    }

}
