package uk.gov.hmcts.reform.idam.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamUserRestorerService {

    private final LauIdamUserService lauService;
    private final RestoreUserService restoreService;
    private final SecurityUtil securityUtil;

    @Getter
    private final RestoreSummary summary = new RestoreSummary();

    @Value("${restorer.batch.size}")
    private int batchSize;
    @Value("${restorer.requests.limit}")
    private int requestsLimit;

    public static final String MARKER = "IDAM_USER_RESTORER";

    public void run() {
        securityUtil.generateTokens();
        int requestsMade = 0;

        while (lauService.hasMore() && requestsMade < requestsLimit) {
            List<DeletionLog> deletedUsers = lauService.fetchDeletedUsers(batchSize);
            log.info("Fetched deleted users {}", deletedUsers.stream().map(DeletionLog::getUserId).toList());
            requestsMade++;

            if (deletedUsers.isEmpty()) {
                break;
            }

            for (DeletionLog deletionLog: deletedUsers) {
                deleteLogEntry(restoreService.restoreUser(deletionLog), deletionLog.getUserId());
            }
        }
    }

    private void deleteLogEntry(boolean successfulRestore, String userId) {
        if (successfulRestore) {
            boolean deletion = lauService.deleteLogEntry(userId);
            if (deletion) {
                summary.addSuccess(userId);
            }
        } else {
            summary.addFailedRestore(userId);
        }
    }
}
