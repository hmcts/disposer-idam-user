package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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

    @Test
    void createRestorerStatisticsShouldReturnFormattedString() {
        RestoreSummary summary = new RestoreSummary();
        // 2023-11-14 22:13:20
        ReflectionTestUtils.setField(summary, "startTime", 1_700_000_000_000L);
        // 70s later - 2023-11-14 22:14:30
        ReflectionTestUtils.setField(summary, "endTime", 1_700_000_070_000L);
        summary.addProcessedNumber(10);
        summary.addSuccess("01");
        summary.addSuccess("02");
        summary.addSuccess("03");
        summary.addFailedToRestoreDueToDuplicateEmail("04");
        summary.addFailedToRestoreDueToNewAccountWithSameEmail("05");
        summary.addFailedToRestoreDueToNewAccountWithSameEmail("06");
        summary.addFailedToRestoreDueToReinstatedAccount("07");
        summary.addFailedToRestoreDueToReinstatedAccount("08");
        summary.addFailedToRestoreDueToReinstatedAccount("09");
        summary.addFailedToRestoreDueToReinstatedAndActiveAccount("10");
        String stats = loggingSummaryUtils.createRestorerStatistics(summary);
        assertThat(stats)
            .contains("2023-11-14 22:13:20")
            .contains("2023-11-14 22:14:30")
            .contains("00:01:10")
            .containsIgnoringWhitespaces("Processed deletion logs: | 10")
            .containsIgnoringWhitespaces("Total restored users: | 3")
            .containsIgnoringWhitespaces("Total failed users: | 7")
            .containsIgnoringWhitespaces("Total 409 due to user id conflict: | 1")
            .containsIgnoringWhitespaces("Total 409 due to archived user id conflict: | 3")
            .containsIgnoringWhitespaces("Total 409 due to email conflict: | 2")
            .containsIgnoringWhitespaces("Total 409 due to archived email conflict: | 1")
            .containsIgnoringWhitespaces("Total failed due to other reasons: | 0");
    }
}
