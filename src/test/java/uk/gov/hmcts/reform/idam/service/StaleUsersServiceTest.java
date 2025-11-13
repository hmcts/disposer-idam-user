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
import java.util.Set;

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
        when(parameterResolver.getStaleUsersBatchSize()).thenReturn(2);
        when(parameterResolver.getCitizenRole()).thenReturn("citizen");
        when(parameterResolver.getCitizenRolesPattern()).thenReturn("letter-");
        when(parameterResolver.getAdditionalIdamCitizenRoles())
            .thenReturn(java.util.Optional.of(Set.of("claimant", "defendant", "divorce-private-beta")));
        when(idamTokenGenerator.getIdamAuthorizationHeader()).thenReturn("Authorization: Bearer token");
    }

    @Test
    void shouldMakeRequestOnFetchStaleUsers() {
        List<UserContent> userContentList = new LinkedList<>();
        userContentList.add(new UserContent("1", List.of("citizen")));
        userContentList.add(new UserContent("2", List.of("defendant")));
        StaleUsersResponse response = new StaleUsersResponse(userContentList, false);

        when(idamClient.getStaleUsers(anyString(), any())).thenReturn(response);

        List<String> staleUsers = staleUsersService.next();
        assertThat(staleUsers).hasSize(1);
        verify(idamClient, times(1)).getStaleUsers(anyString(), any());
        assertThat(staleUsersService.hasNext()).isTrue();
    }

    @Test
    void shouldFilterStaleUsersByCitizenRoles() {
        final List<UserContent> userContentList = new LinkedList<>();
        userContentList.add(new UserContent("001", Arrays.asList("citizen", "defendant", "claimant", "letter-123")));
        userContentList.add(new UserContent("002", Arrays.asList("CITIZEN", "DEFENDANT", "CLAIMANT")));
        userContentList.add(new UserContent("003", Arrays.asList("CITIZEN", "DEFENDANT", "CLAIMANT", "LETTER-123")));
        userContentList.add(new UserContent("004", List.of("CITIZEN", "LETTER-c1727ce3-33f8-4b91-a50d-d1c1dcd346c7")));
        userContentList.add(new UserContent("005", List.of("LETTER-1234")));
        userContentList.add(new UserContent("006", List.of("defendant")));
        userContentList.add(new UserContent("007", List.of("citizen")));
        userContentList.add(new UserContent("008", List.of("CiTiZeN", "dEfEnDaNt")));
        userContentList.add(new UserContent("009", Arrays.asList("CitIZen", "judge")));
        userContentList.add(new UserContent("010", null));
        userContentList.add(new UserContent("011", emptyList()));
        userContentList.add(new UserContent("012", List.of("CITIZEN")));
        userContentList.add(new UserContent("013", List.of("CITIZEN", "defendant")));
        userContentList.add(new UserContent("014", List.of("JUROR")));
        userContentList.add(new UserContent("015", List.of("citizen", "letter-c1727ce3-33f8-4b91-a50d-d1c1dcd346c7")));
        userContentList.add(new UserContent("016", List.of("claimant", "defendant", "divorce-private-beta")));
        final StaleUsersResponse response = new StaleUsersResponse(userContentList, false);

        when(idamClient.getStaleUsers(anyString(), any())).thenReturn(response);

        final List<String> staleUsers = staleUsersService.next();
        assertThat(staleUsers)
            .hasSize(9)
            .containsAll(List.of("001", "002", "003", "004", "007", "008", "012", "013", "015"));
    }

    @Test
    void shouldFilterOnlyOnMandatoryRoleIfOtherNotProvided() {
        when(parameterResolver.getCitizenRolesPattern()).thenReturn(null);
        when(parameterResolver.getAdditionalIdamCitizenRoles())
            .thenReturn(java.util.Optional.empty());
        final List<UserContent> userContentList = new LinkedList<>();
        userContentList.add(new UserContent("001", Arrays.asList("citizen", "defendant", "claimant", "letter-123")));
        userContentList.add(new UserContent("004", List.of("CITIZEN", "LETTER-c1727ce3-33f8-4b91-a50d-d1c1dcd346c7")));
        userContentList.add(new UserContent("005", List.of("LETTER-1234")));
        userContentList.add(new UserContent("006", List.of("defendant")));
        userContentList.add(new UserContent("007", List.of("citizen")));
        userContentList.add(new UserContent("008", List.of("CiTiZeN", "dEfEnDaNt")));
        userContentList.add(new UserContent("009", Arrays.asList("CitIZen", "judge")));
        userContentList.add(new UserContent("010", null));
        userContentList.add(new UserContent("011", emptyList()));
        userContentList.add(new UserContent("012", List.of("CITIZEN")));
        userContentList.add(new UserContent("013", List.of("CITIZEN", "defendant")));
        userContentList.add(new UserContent("014", List.of("JUROR")));
        userContentList.add(new UserContent("015", List.of("citizen", "letter-c1727ce3-33f8-4b91-a50d-d1c1dcd346c7")));
        userContentList.add(new UserContent("016", List.of("claimant", "defendant", "divorce-private-beta")));
        final StaleUsersResponse response = new StaleUsersResponse(userContentList, false);

        when(idamClient.getStaleUsers(anyString(), any())).thenReturn(response);

        final List<String> staleUsers = staleUsersService.next();
        assertThat(staleUsers)
            .hasSize(2)
            .containsAll(List.of("007", "012"));

    }

}
