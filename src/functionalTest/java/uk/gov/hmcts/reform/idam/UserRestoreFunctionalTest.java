package uk.gov.hmcts.reform.idam;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.helpers.IdamUserDataProvider;
import uk.gov.hmcts.reform.idam.helpers.LauDeletionLogEntryProvider;
import uk.gov.hmcts.reform.idam.helpers.LauIdamBackendServiceProvider;
import uk.gov.hmcts.reform.idam.service.IdamUserRestorerService;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class UserRestoreFunctionalTest {

    @Inject
    IdamUserRestorerService idamUserRestoreService;

    @Inject
    private IdamUserDataProvider idamUserDataProvider;

    @Inject
    private LauDeletionLogEntryProvider lauDeletionLogEntryProvider;

    @Inject
    LauIdamBackendServiceProvider lauIdamBackendServiceProvider;

    @Inject
    private RestoreSummary restoreSummary;

    List<String> logEntryUserIds = new ArrayList<>();

    @Test
    @DirtiesContext
    void givenDeletedUserExistsThenShouldAbleToRestoreDeletedUsers() {
        List<DeletionLog> lauDeletionLogs = lauDeletionLogEntryProvider.createDeletionLogLau();
        assertEquals("Deletion Log entry has not created", 1, lauDeletionLogs.size());
        logEntryUserIds.add(lauDeletionLogs.get(0).getUserId());

        idamUserRestoreService.run();

        with().await()
            .untilAsserted(() -> {
                List<String> userIds = restoreSummary.getSuccessful();
                assertEquals("User has not been restored successfully", 1, userIds.size());
            });
    }

    @Test
    @DirtiesContext
    void givenDeletedUserExistsInIdamThenFailedToRestoreDueToReinstatedAndActiveAccount() {
        String userId = idamUserDataProvider.createIdamUser();
        List<DeletionLog> deletionLogs = lauDeletionLogEntryProvider.postDeletedLogEntryWithExistingUserId(userId, 4);
        assertEquals("Deletion Log entry has not created", 1, deletionLogs.size());
        logEntryUserIds.add(deletionLogs.get(0).getUserId());

        idamUserRestoreService.run();

        with().await()
            .untilAsserted(() -> {
                List<String> failedUserIds = restoreSummary.getFailedToRestoreDueToReinstatedAndActiveAccount();
                assertEquals("User has not been restored successfully", 1, failedUserIds.size());
            });
    }

    @Test
    @DirtiesContext
    void givenDeletedUserExistsInIdamAndRetiredThenFailedToRestoreDueToReinstatedAccount() {
        String userId = idamUserDataProvider.createAndRetireIdamUser();
        List<DeletionLog> deletionLogs = lauDeletionLogEntryProvider.postDeletedLogEntryWithExistingUserId(userId, 6);
        assertEquals("Deletion Log entry has not created", 1, deletionLogs.size());
        logEntryUserIds.add(deletionLogs.get(0).getUserId());

        idamUserRestoreService.run();

        with().await()
            .untilAsserted(() -> {
                List<String> failedUserIds = restoreSummary.getFailedToRestoreDueToReinstatedAccount();
                assertEquals("User has not been restored successfully", 1, failedUserIds.size());
            });
    }

    @Test
    @DirtiesContext
    void givenDeletedUserExistsInIdamThenFailedToRestoreDueToNewAccountWithSameEmail() {
        String emailAddress = idamUserDataProvider.createIdamUserAndReturnEmailAddress();
        List<DeletionLog> deletionLogs = lauDeletionLogEntryProvider
            .postDeletedLogEntryWithExistingEmail(emailAddress, 8);
        assertEquals("Deletion Log entry has not created", 1, deletionLogs.size());
        logEntryUserIds.add(deletionLogs.get(0).getUserId());

        idamUserRestoreService.run();

        with().await()
            .untilAsserted(() -> {
                List<String> failedUserIds = restoreSummary.getFailedToRestoreDueToNewAccountWithSameEmail();
                assertEquals("There should be 1 user fail to restore", 1, failedUserIds.size());
            });
    }

    @Test
    @DirtiesContext
    void givenDeletedUserExistsInIdamWithEmailAndRetiredThenFailedToRestoreDueToDuplicateEmail() {
        String emailAddress = idamUserDataProvider.createAndRetireIdamUserAndReturnEmailAddress();
        List<DeletionLog> deletionLogs = lauDeletionLogEntryProvider
            .postDeletedLogEntryWithExistingEmail(emailAddress, 10);

        assertEquals("Deletion Log entry has not created", 1, deletionLogs.size());

        logEntryUserIds.add(deletionLogs.get(0).getUserId());

        idamUserRestoreService.run();

        with().await()
            .untilAsserted(() -> {
                List<String> failedUserIds = restoreSummary.getFailedToRestoreDueToDuplicateEmail();
                assertEquals("There should be 1 user fail to restore", 1, failedUserIds.size());
            });
    }

    @AfterEach
    public void teardown() {
        if (!logEntryUserIds.isEmpty()) {
            for (String logEntryUserId : logEntryUserIds) {
                int statusCode = lauIdamBackendServiceProvider.deleteLogEntry(logEntryUserId);
                assertEquals("Log entry not deleted", 204, statusCode);
            }
        }
    }

}
