package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static final int FAILED_DELETIONS = 123;

    @Test
    void shouldCallLogSummary() {
        when(parameterResolver.getIsSimulation()).thenReturn(true);
        when(parameterResolver.getBatchSize()).thenReturn(100);
        when(parameterResolver.getRequestLimit()).thenReturn(1000);
        loggingSummaryUtils.logSummary(
            Long.parseLong(START_TIME),
            Long.parseLong(END_TIME),
            PROCESSED_USERS,
            DELETED_USERS,
            FAILED_DELETIONS
        );
        verify(parameterResolver, times(1)).getIsSimulation();
        verify(parameterResolver, times(1)).getBatchSize();
        verify(parameterResolver, times(1)).getRequestLimit();
    }

    @Test
    void durationFromLongShouldCorrectlyFormat() {
        String result = loggingSummaryUtils.getDurationFromLong(1_000L);
        assertThat(result).isEqualTo("00:00:01");
        result = loggingSummaryUtils.getDurationFromLong(10_000L);
        assertThat(result).isEqualTo("00:00:10");
        result = loggingSummaryUtils.getDurationFromLong(60_000L);
        assertThat(result).isEqualTo("00:01:00");
        result = loggingSummaryUtils.getDurationFromLong(61_000L);
        assertThat(result).isEqualTo("00:01:01");
        result = loggingSummaryUtils.getDurationFromLong(610_000L);
        assertThat(result).isEqualTo("00:10:10");
        result = loggingSummaryUtils.getDurationFromLong(3_600_000L);
        assertThat(result).isEqualTo("01:00:00");
        result = loggingSummaryUtils.getDurationFromLong(3_661_000L);
        assertThat(result).isEqualTo("01:01:01");
    }

}
