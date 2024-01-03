package uk.gov.hmcts.reform.idam.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateUserSummaryTest {

    DuplicateUserSummary duplicateUserSummary = new DuplicateUserSummary();

    @Test
    void setStartTimeShouldSetStartTime() {
        duplicateUserSummary.setStartTime();
        long currentTime = System.currentTimeMillis();
        assertThat(duplicateUserSummary.getStartTime()).isCloseTo(currentTime, Offset.offset(100L));
    }

    @Test
    void setEndTimeShouldSetEndTime() {
        duplicateUserSummary.setEndTime();
        long currentTime = System.currentTimeMillis();
        assertThat(duplicateUserSummary.getEndTime()).isCloseTo(currentTime, Offset.offset(100L));
    }

    @Test
    void increaseMatchedIdsShoudIncreaseByOne() {
        duplicateUserSummary.increaseMatchedIds();
        duplicateUserSummary.increaseMatchedIds();
        assertThat(duplicateUserSummary.getEmailAndIdsMatch()).isEqualTo(2);
    }

    @Test
    void increaseEmailMultipleIdsShouldIncreaseByOne() {
        duplicateUserSummary.increaseEmailMultipleIds();
        duplicateUserSummary.increaseEmailMultipleIds();
        assertThat(duplicateUserSummary.getMultipleIdsOnEmail()).isEqualTo(2);
    }

    @Test
    void increaseNoMatchesShouldIncreaseByOne() {
        duplicateUserSummary.increaseNoMatches();
        duplicateUserSummary.increaseNoMatches();
        assertThat(duplicateUserSummary.getNoUserInIdam()).isEqualTo(2);
    }

    @Test
    void increaseMultipleMatchesShouldIncreaseByOne() {
        duplicateUserSummary.increaseMultipleMatches();
        duplicateUserSummary.increaseMultipleMatches();
        assertThat(duplicateUserSummary.getMultipleUsersInIdamMatchEmail()).isEqualTo(2);
    }
}
