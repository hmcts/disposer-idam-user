package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.config.CcdProperties;
import uk.gov.hmcts.reform.idam.config.StaleUsersProperties;
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

    private CcdProperties ccdProperties;

    private StaleUsersProperties staleUsersProperties;

    @Mock
    private Clock clock;

    @Mock
    private ListUtils listUtils;

    private IdamUserDisposerService service;

    @BeforeEach
    void setUp() {
        staleUsersProperties = new StaleUsersProperties();
        StaleUsersProperties.Requests requests = new StaleUsersProperties.Requests();
        requests.setLimit(10);
        staleUsersProperties.setRequests(requests);
        staleUsersProperties.setCutOffTime(LocalTime.of(6, 0));
        ccdProperties = new CcdProperties();
        CcdProperties.RoleAssignment roleAssignment = new CcdProperties.RoleAssignment();
        roleAssignment.setBatchSize(10);
        ccdProperties.setRoleAssignment(roleAssignment);

        when(clock.instant())
            .thenReturn(Instant.parse("2025-01-15T20:05:00.000Z")) // applicationStartTime
            .thenReturn(Instant.parse("2025-01-15T23:32:15.123Z")); // first check of current time
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        service = new IdamUserDisposerService(
            staleUsersService,
            userRoleService,
            deleteUserService,
            staleUsersProperties,
            ccdProperties,
            listUtils,
            clock
        );
    }

    @Test
    void shouldRunAtLeastOnce() {
        when(staleUsersService.hasNext()).thenReturn(true).thenReturn(false);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.next()).thenReturn(allProcessedStaleUserIds);

        when(listUtils.partition(any(), anyInt())).thenReturn(List.of(allProcessedStaleUserIds));

        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);


        service.run();

        verify(staleUsersService, times(1)).next();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
    }

    @Test
    void shouldRunMultipleTimes() {
        when(staleUsersService.hasNext())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.next()).thenReturn(allProcessedStaleUserIds);
        when(listUtils.partition(any(), anyInt())).thenReturn(List.of(allProcessedStaleUserIds));
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(staleUsersService, times(3)).next();
        verify(userRoleService, times(3)).filterUsersWithRoles(any());
        verify(deleteUserService, times(3)).deleteUsers(any());
    }

    @Test
    void shouldStopWhenRequestLimitIsReached() {
        when(staleUsersService.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        staleUsersProperties.getRequests().setLimit(1);

        List<String> allProcessedStaleUserIds = Arrays.asList("45678", "67899", "78905");
        when(staleUsersService.next()).thenReturn(allProcessedStaleUserIds);
        when(listUtils.partition(any(), anyInt())).thenReturn(List.of(allProcessedStaleUserIds));
        List<String> allRemovedStaleUserIds = Arrays.asList("45678", "67899");
        when(userRoleService.filterUsersWithRoles(allProcessedStaleUserIds)).thenReturn(allRemovedStaleUserIds);

        service.run();

        verify(staleUsersService, times(1)).next();
        verify(userRoleService, times(1)).filterUsersWithRoles(any());
        verify(deleteUserService, times(1)).deleteUsers(any());
    }

    @ParameterizedTest
    @CsvSource({
        "2025-01-15T20:05:00.000Z, 2025-01-15T23:59:43.000Z, 2025-01-16T07:15:43.000Z",
        "2025-01-16T00:05:00.000Z, 2025-01-16T04:59:43.000Z, 2025-01-16T09:15:30.000Z",
        "2025-01-16T08:05:00.000Z, 2025-01-16T15:59:43.000Z, 2025-01-17T07:10:00.000Z",
        "2025-01-16T20:05:00.000Z, 2025-01-17T03:45:43.000Z, 2025-01-17T07:10:00.000Z"
    })
    void shouldRunBasedOnTime(String startTime, String currentTime1, String currentTime2) {
        when(clock.instant())
            .thenReturn(Instant.parse(startTime)) // applicationStartTime
            .thenReturn(Instant.parse(currentTime1))
            .thenReturn(Instant.parse(currentTime2));
        when(staleUsersService.hasNext()).thenReturn(true);

        service.run();

        verify(staleUsersService, times(1)).next();
    }

    @Test
    void shouldNotRunBasedOnTime() {
        when(clock.instant())
            .thenReturn(Instant.parse("2025-01-15T20:05:00.000Z")) // applicationStartTime
            .thenReturn(Instant.parse("2025-01-16T15:59:43.000Z"));

        service.run();

        verify(staleUsersService, times(0)).next();
    }
}
