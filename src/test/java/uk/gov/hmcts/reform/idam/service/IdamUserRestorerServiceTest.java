package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdamUserRestorerServiceTest {

    @Mock
    LauIdamUserService lauService;

    @Mock
    RestoreUserService restoreService;

    @Mock
    SecurityUtil securityUtil;

    @Spy
    RestoreSummary restoreSummary;

    DeletionLog log = DeletionLog.builder().build();

    @InjectMocks
    private IdamUserRestorerService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "requestsLimit", 10);
    }

    @Test
    void shouldRunAtLeastOnce() {
        when(lauService.hasMoreRecords()).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers()).thenReturn(List.of(log));

        service.run();

        verify(lauService, times(1)).fetchDeletedUsers();
        verify(restoreService, times(1)).restoreUser(any());

    }

    @Test
    void shouldRunMultipleTimes() {
        when(lauService.hasMoreRecords()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers()).thenReturn(List.of(log, log));

        service.run();

        verify(lauService, times(2)).fetchDeletedUsers();
        verify(restoreService, times(4)).restoreUser(any());
    }

    @Test
    void shouldNotCallDeleteLogEntryIfUnsuccessfulRestore() {
        when(lauService.hasMoreRecords()).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers()).thenReturn(List.of(log, log));

        service.run();

        verify(lauService, times(1)).fetchDeletedUsers();
        verify(restoreService, times(2)).restoreUser(any());
    }

    @Test
    void shouldCallOnlyForSuccessfulRestore() {
        when(lauService.hasMoreRecords()).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers()).thenReturn(List.of(log, log));

        service.run();

        verify(lauService, times(1)).fetchDeletedUsers();
        verify(restoreService, times(2)).restoreUser(any());
    }

    @Test
    void shouldStopIfLauDeletedUsersFetchReturnsEmpty() {
        when(lauService.hasMoreRecords()).thenReturn(true).thenReturn(true);
        when(lauService.fetchDeletedUsers()).thenReturn(List.of());

        service.run();

        verify(restoreService, times(0)).restoreUser(any());
    }

    @Test
    void shouldStopAfterRequestsLimitIsReached() {
        when(lauService.hasMoreRecords()).thenReturn(true);
        when(lauService.fetchDeletedUsers()).thenReturn(List.of(log, log));

        service.run();

        verify(lauService, times(10)).fetchDeletedUsers();
        verify(restoreService, times(20)).restoreUser(any());
    }
}
