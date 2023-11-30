package uk.gov.hmcts.reform.idam.util;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestoreSummaryTest {

    @Test
    void shouldAddSuccessfulRestore() {
        RestoreSummary summary = new RestoreSummary();
        summary.addSuccess("00001");
        assertThat(summary.getSuccessful()).hasSize(1);
        assertThat(summary.getSuccessful().get(0)).isEqualTo("00001");
    }

    @Test
    void shouldAddFailedToRestoreDueToNewAccountWithSameEmail() {
        RestoreSummary summary = new RestoreSummary();
        summary.addFailedToRestoreDueToNewAccountWithSameEmail("00001");
        assertThat(summary.getFailedToRestoreDueToNewAccountWithSameEmail()).hasSize(1);
        assertThat(summary.getFailedToRestoreDueToNewAccountWithSameEmail().get(0)).isEqualTo("00001");
    }

    @Test
    void shouldAddFailedToRestoreDueToReinstatedAccount() {
        RestoreSummary summary = new RestoreSummary();
        summary.addFailedToRestoreDueToReinstatedAccount("00001");
        assertThat(summary.getFailedToRestoreDueToReinstatedAccount()).hasSize(1);
        assertThat(summary.getFailedToRestoreDueToReinstatedAccount().get(0)).isEqualTo("00001");
    }

    @Test
    void shouldAddFailedToRestoreDueToReinstatedAndActiveAccount() {
        RestoreSummary summary = new RestoreSummary();
        summary.addFailedToRestoreDueToReinstatedAndActiveAccount("00001");
        assertThat(summary.getFailedToRestoreDueToReinstatedAndActiveAccount()).hasSize(1);
        assertThat(summary.getFailedToRestoreDueToReinstatedAndActiveAccount().get(0)).isEqualTo("00001");
    }

    @Test
    void shouldAddFailedToRestoreDueToDuplicateEmail() {
        RestoreSummary summary = new RestoreSummary();
        summary.addFailedToRestoreDueToDuplicateEmail("00001");
        assertThat(summary.getFailedToRestoreDueToDuplicateEmail()).hasSize(1);
        assertThat(summary.getFailedToRestoreDueToDuplicateEmail().get(0)).isEqualTo("00001");
    }
}
