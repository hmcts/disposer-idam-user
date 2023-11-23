package uk.gov.hmcts.reform.idam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    DeletionLog log = DeletionLog.builder().build();

    @InjectMocks
    private IdamUserRestorerService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "requestsLimit", 10);
        ReflectionTestUtils.setField(service, "batchSize", 10);
    }

    @Test
    void shouldRunAtLeastOnce() {
        when(lauService.hasMore()).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers(10)).thenReturn(List.of(log));
        when(lauService.deleteLogEntry(any())).thenReturn(true);
        when(restoreService.restoreUser(any())).thenReturn(true);

        service.run();
        var summary = service.getSummary();

        verify(lauService, times(1)).fetchDeletedUsers(10);
        verify(restoreService, times(1)).restoreUser(any());
        verify(lauService, times(1)).deleteLogEntry(any());
        assertThat(summary.getSuccessful()).hasSize(1);
    }

    @Test
    void shouldRunMultipleTimes() {
        when(lauService.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers(10)).thenReturn(List.of(log, log));
        when(lauService.deleteLogEntry(any())).thenReturn(true);
        when(restoreService.restoreUser(any())).thenReturn(true);

        service.run();
        var summary = service.getSummary();

        verify(lauService, times(2)).fetchDeletedUsers(10);
        verify(restoreService, times(4)).restoreUser(any());
        verify(lauService, times(4)).deleteLogEntry(any());
        assertThat(summary.getSuccessful()).hasSize(4);
        assertThat(summary.getFailedToRestore()).isEmpty();
    }

    @Test
    void shouldNotCallDeleteLogEntryIfUnsuccessfulRestore() {
        when(lauService.hasMore()).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers(10)).thenReturn(List.of(log, log));
        when(restoreService.restoreUser(any())).thenReturn(false);

        service.run();
        var summary = service.getSummary();

        verify(lauService, times(1)).fetchDeletedUsers(10);
        verify(restoreService, times(2)).restoreUser(any());
        verify(lauService, times(0)).deleteLogEntry(any());
        assertThat(summary.getSuccessful()).isEmpty();
        assertThat(summary.getFailedToRestore()).hasSize(2);
    }

    @Test
    void shouldCallOnlyForSuccessfulRestore() {
        when(lauService.hasMore()).thenReturn(true).thenReturn(false);
        when(lauService.fetchDeletedUsers(10)).thenReturn(List.of(log, log));
        when(lauService.deleteLogEntry(any())).thenReturn(true);
        when(restoreService.restoreUser(any())).thenReturn(false).thenReturn(true);

        service.run();
        var summary = service.getSummary();

        verify(lauService, times(1)).fetchDeletedUsers(10);
        verify(restoreService, times(2)).restoreUser(any());
        verify(lauService, times(1)).deleteLogEntry(any());
        assertThat(summary.getSuccessful()).hasSize(1);
        assertThat(summary.getFailedToRestore()).hasSize(1);
    }

    @Test
    void shouldStopIfLauDeletedUsersFetchReturnsEmpty() {
        when(lauService.hasMore()).thenReturn(true).thenReturn(true);
        when(lauService.fetchDeletedUsers(10)).thenReturn(List.of());
        service.run();
        var summary = service.getSummary();
        verify(restoreService, times(0)).restoreUser(any());
        verify(lauService, times(0)).deleteLogEntry(any());
        assertThat(summary.getSuccessful()).isEmpty();
        assertThat(summary.getFailedToRestore()).isEmpty();
    }

    @Test
    void shouldStopAfterRequestsLimitIsReached() {
        when(lauService.hasMore()).thenReturn(true);
        when(lauService.fetchDeletedUsers(10)).thenReturn(List.of(log, log));
        when(lauService.deleteLogEntry(any())).thenReturn(true);
        when(restoreService.restoreUser(any())).thenReturn(true);

        service.run();
        var summary = service.getSummary();
        verify(lauService, times(10)).fetchDeletedUsers(10);
        verify(restoreService, times(20)).restoreUser(any());
        verify(lauService, times(20)).deleteLogEntry(any());
        assertThat(summary.getSuccessful()).hasSize(20);
        assertThat(summary.getFailedToRestore()).isEmpty();
    }
}
