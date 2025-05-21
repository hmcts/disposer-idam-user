package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.util.ListUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private Clock clock;

    @Mock
    private ListUtils listUtils;

    @InjectMocks
    private IdamUserDisposerService service;

    @BeforeEach
    void setUp() {
        when(clock.instant())
            .thenReturn(Instant.parse("2025-01-15T20:05:00.000Z")) // applicationStartTime
            .thenReturn(Instant.parse("2025-01-15T23:32:15.123Z")); // first check of current time
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(parameterResolver.getCutOffTime()).thenReturn(LocalTime.of(6, 0));
    }

    @Test
    void shouldRunAtLeastOnce() {
        when(staleUsersService.hasFinished()).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(10);
        when(parameterResolver.getRasBatchSize()).thenReturn(10);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);

        when(listUtils.partition(any(), anyInt())).thenReturn(List.of(allProcessedStaleUserIds));

        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);


        service.run();

        verify(staleUsersService, times(1)).fetchStaleUsers();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
    }

    @Test
    void shouldRunMultipleTimes() {
        when(staleUsersService.hasFinished()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(10);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);
        when(listUtils.partition(any(), anyInt())).thenReturn(List.of(allProcessedStaleUserIds));
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(staleUsersService, times(3)).fetchStaleUsers();
        verify(userRoleService, times(3)).filterUsersWithRoles(any());
        verify(deleteUserService, times(3)).deleteUsers(any());
    }

    @Test
    void shouldStopWhenRequestLimitIsReached() {
        when(staleUsersService.hasFinished()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(1);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);
        when(listUtils.partition(any(), anyInt())).thenReturn(List.of(allProcessedStaleUserIds));
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(staleUsersService, times(1)).fetchStaleUsers();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
    }

    @ParameterizedTest
    @CsvSource({
        "2025-01-15T20:05:00.000Z, 2025-01-15T23:59:43.000Z, 2025-01-16T07:15:43.000Z, 1",
        "2025-01-15T20:05:00.000Z, 2025-01-16T15:59:43.000Z, 2025-01-15T23:15:30.000Z, 0",
        "2025-01-16T00:05:00.000Z, 2025-01-16T04:59:43.000Z, 2025-01-16T09:15:30.000Z, 1",
        "2025-01-16T08:05:00.000Z, 2025-01-16T15:59:43.000Z, 2025-01-17T07:10:00.000Z, 1",
        "2025-01-16T20:05:00.000Z, 2025-01-17T03:45:43.000Z, 2025-01-17T07:10:00.000Z, 1"
    })
    void shouldRunOrNotBasedOnTime(String startTime, String currentTime1, String currentTime2, int invocations) {
        when(clock.instant())
            .thenReturn(Instant.parse(startTime)) // applicationStartTime
            .thenReturn(Instant.parse(currentTime1))
            .thenReturn(Instant.parse(currentTime2));
        when(parameterResolver.getRequestLimit()).thenReturn(10);

        service.run();

        verify(staleUsersService, times(invocations)).fetchStaleUsers();
    }
}
