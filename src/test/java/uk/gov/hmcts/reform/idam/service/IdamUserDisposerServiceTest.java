package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private IdamUserDisposerService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "applicationStartTime", LocalDateTime.parse("2025-01-15T20:14:43"));
        when(clock.instant()).thenReturn(Instant.parse("2025-01-15T23:34:00.000Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(parameterResolver.getRunBefore()).thenReturn(LocalTime.of(7, 0));
    }

    @Test
    void shouldRunAtLeastOnce() {
        when(staleUsersService.hasFinished()).thenReturn(true);
        when(parameterResolver.getRequestLimit()).thenReturn(10);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.fetchStaleUsers()).thenReturn(allProcessedStaleUserIds);
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
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(staleUsersService, times(1)).fetchStaleUsers();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
    }

    @ParameterizedTest
    @CsvSource({
        "2025-01-15T15:59:43.000Z, 0",
        "2025-01-14T23:15:30.000Z, 1",
        "2025-01-15T03:15:30.000Z, 1",
        "2025-01-15T00:00:00.000Z, 1",
        "2025-01-15T12:00:00.000Z, 0",
    })
    void shouldRunOrNotBasedOnTime(String currentTime, int invocations) {
        ReflectionTestUtils.setField(service, "applicationStartTime", LocalDateTime.parse("2025-01-14T20:14:43"));
        when(clock.instant()).thenReturn(Instant.parse(currentTime));
        when(parameterResolver.getRequestLimit()).thenReturn(1);

        service.run();

        verify(staleUsersService, times(invocations)).fetchStaleUsers();
    }
}
