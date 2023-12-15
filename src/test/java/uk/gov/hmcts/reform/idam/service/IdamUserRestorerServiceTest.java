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
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class IdamUserRestorerServiceTest {

    @Mock
    LauIdamUserService lauService;

    @Mock
    RestoreUserService restoreService;

    @Spy
    RestoreSummary restoreSummary;

    @Mock
    LoggingSummaryUtils summaryUtils;

    DeletionLog deletionLog = DeletionLog.builder().build();

    @InjectMocks
    private IdamUserRestorerService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "requestsLimit", 10);
        ReflectionTestUtils.setField(service, "batchSize", 1);
        ReflectionTestUtils.setField(service, "startPage", 1);
    }

    @Test
    void shouldCallRetrieveDeletedUsers() {
        service.run();

        verify(lauService, times(1)).retrieveDeletedUsers(service,10, 1, 1);
        verify(summaryUtils, times(1)).createRestorerStatistics(any());
    }

    @Test
    void consumeLauDeletedUsersShouldCallRestoreService() {
        service.consumeLauDeletedUsers(List.of(deletionLog));

        verify(restoreService, times(1)).restoreUser(any());
        verify(restoreSummary, times(1)).increaseRequestsMade();
        verify(restoreSummary, times(1)).addProcessedNumber(1);
    }

    @Test
    void consumeLauDeletedUsersShouldNotCallRestoreServiceOnEmptyList() {
        service.consumeLauDeletedUsers(List.of());

        verifyNoInteractions(restoreService);
        verify(restoreSummary, times(1)).increaseRequestsMade();
        verify(restoreSummary, times(1)).addProcessedNumber(0);
    }
}
