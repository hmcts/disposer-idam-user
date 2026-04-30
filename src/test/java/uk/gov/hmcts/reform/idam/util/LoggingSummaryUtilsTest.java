package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.idam.config.CcdProperties;
import uk.gov.hmcts.reform.idam.config.StaleUsersProperties;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class LoggingSummaryUtilsTest {

    private LoggingSummaryUtils loggingSummaryUtils;

    private StaleUsersProperties staleUsersProperties;
    private CcdProperties ccdProperties;

    private static final String START_TIME = "1696954607339";
    private static final String END_TIME = "1696954607360";
    private static final int PROCESSED_USERS = 1000;
    private static final int DELETED_USERS = 900;

    private static final int FAILED_DELETIONS = 123;

    @BeforeEach
    void setUp() {
        staleUsersProperties = new StaleUsersProperties();
        staleUsersProperties.setBatchSize(123);
        StaleUsersProperties.Requests requests = new StaleUsersProperties.Requests();
        requests.setLimit(1000);
        staleUsersProperties.setRequests(requests);
        staleUsersProperties.setSimulation(true);
        ccdProperties = new CcdProperties();
        CcdProperties.RoleAssignment roleAssignment = new CcdProperties.RoleAssignment();
        roleAssignment.setBatchSize(100);
        ccdProperties.setRoleAssignment(roleAssignment);

        loggingSummaryUtils = new LoggingSummaryUtils(staleUsersProperties, ccdProperties);
    }

    @Test
    void shouldCallLogSummary(CapturedOutput output) {
        loggingSummaryUtils.logSummary(
            Long.parseLong(START_TIME),
            Long.parseLong(END_TIME),
            PROCESSED_USERS,
            DELETED_USERS,
            FAILED_DELETIONS
        );

        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(output.getOut()).contains("Is Simulation Mode :        | true");
            assertThat(output.getOut()).contains("Stale Users Batch Size :    | 123");
            assertThat(output.getOut()).contains("RAS Batch Size :            | 100");
            assertThat(output.getOut()).contains("Request Limit :             | 1000");
        });
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
