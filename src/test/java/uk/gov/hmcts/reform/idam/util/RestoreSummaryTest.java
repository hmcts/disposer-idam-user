package uk.gov.hmcts.reform.idam.util;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestoreSummaryTest {

    @Test
    void addSuccessShouldAdduserId() {
        RestoreSummary summary = new RestoreSummary();
        summary.addSuccess("00001");
        assertThat(summary.getSuccessful()).hasSize(1);
        assertThat(summary.getSuccessful().get(0)).isEqualTo("00001");
    }

    @Test
    void addFailedRestoreShouldAdduserId() {
        RestoreSummary summary = new RestoreSummary();
        summary.addFailedRestore("00001");
        assertThat(summary.getFailedToRestore()).hasSize(1);
        assertThat(summary.getFailedToRestore().get(0)).isEqualTo("00001");
    }

}
