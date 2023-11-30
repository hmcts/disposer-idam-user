package uk.gov.hmcts.reform.idam.service;

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
    private final RestoreSummary restoreSummary;

    @Value("${restorer.requests.limit}")
    private int requestsLimit;

    public static final String MARKER = "IDAM_USER_RESTORER";

    public void run() {

        restoreSummary.setStartTime();

        securityUtil.generateTokens();
        int requestsMade = 0;

        while (lauService.hasMoreRecords() && requestsMade < requestsLimit) {
            List<DeletionLog> deletedUsers = lauService.fetchDeletedUsers();
            log.info("Fetched deleted users {}", deletedUsers.stream().map(DeletionLog::getUserId).toList());
            requestsMade++;

            if (deletedUsers.isEmpty()) {
                break;
            }

            for (DeletionLog deletionLog : deletedUsers) {
                restoreService.restoreUser(deletionLog);
            }

            restoreSummary.setEndTime();
        }
    }
}
