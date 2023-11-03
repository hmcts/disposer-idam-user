package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdamUserDisposerServiceTest {

    @Mock
    private StaleUsersService staleUsersService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private DeleteUserService deleteUserService;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private LoggingSummaryUtils loggingSummaryUtils;

    @InjectMocks
    private IdamUserDisposerService service;

    @Test
    void shouldRunAtLeastOnce() {
        when(staleUsersService.hasFinished()).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(staleUsersService.getTotalStaleUsers()).thenReturn(3);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(securityUtil, times(1)).generateTokens();
        verify(staleUsersService, times(1)).fetchStaleUsers();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
        verify(loggingSummaryUtils, times(1))
            .logSummary(anyLong(), anyLong(), eq(3), eq(2), eq(0));
    }

    @Test
    void shouldRunMultipleTimes() {
        when(staleUsersService.hasFinished()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(staleUsersService.getTotalStaleUsers()).thenReturn(9);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(securityUtil, times(1)).generateTokens();
        verify(staleUsersService, times(3)).fetchStaleUsers();
        verify(userRoleService, times(3)).filterUsersWithRoles(any());
        verify(deleteUserService, times(3)).deleteUsers(any());
        verify(loggingSummaryUtils, times(1))
            .logSummary(anyLong(), anyLong(), eq(9),eq(6), eq(0));
    }

    @Test
    void shouldStopWhenRequestLimitIsReached() {
        when(staleUsersService.hasFinished()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(1);
        when(staleUsersService.getTotalStaleUsers()).thenReturn(3);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(securityUtil, times(1)).generateTokens();
        verify(staleUsersService, times(1)).fetchStaleUsers();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
        verify(loggingSummaryUtils, times(1))
            .logSummary(anyLong(), anyLong(), eq(3), eq(2), eq(0));
    }

}
