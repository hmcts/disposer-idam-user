package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingSummaryUtilsTest {

    @InjectMocks
    private LoggingSummaryUtils loggingSummaryUtils;

    @Mock
    private ParameterResolver parameterResolver;

    @Mock
    private IdamUserDisposerService idamUserDisposerService;

    private static final String START_TIME = "1696954607339";
    private static final String END_TIME = "1696954607360";
    private static final int PROCESSED_USERS = 1000;
    private static final int DELETED_USERS = 900;

    @Test
    void shouldCallLogSummary() {
        when(parameterResolver.getIsSimulation()).thenReturn(true);
        when(parameterResolver.getBatchSize()).thenReturn(100);
        when(parameterResolver.getRequestLimit()).thenReturn(1000);
        loggingSummaryUtils.logSummary(Long.parseLong(START_TIME),Long.parseLong(END_TIME),
                                       PROCESSED_USERS,DELETED_USERS);
        verify(parameterResolver, times(1)).getIsSimulation();
        verify(parameterResolver, times(1)).getBatchSize();
        verify(parameterResolver, times(1)).getRequestLimit();
    }
}
