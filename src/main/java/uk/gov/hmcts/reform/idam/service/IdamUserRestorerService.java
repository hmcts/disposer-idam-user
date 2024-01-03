package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamUserRestorerService implements LauDeletedUsersConsumer {

    private final LauIdamUserService lauService;
    private final RestoreUserService restoreService;
    private final RestoreSummary restoreSummary;
    private final LoggingSummaryUtils summaryUtils;

    @Value("${restorer.requests.limit}")
    private int requestsLimit;

    @Value("${restorer.batch.size}")
    private int batchSize;

    @Value("${restorer.start.page}")
    private int startPage;

    public static final String MARKER = "IDAM_USER_RESTORER";

    public void run() {
        restoreSummary.setStartTime();
        lauService.retrieveDeletedUsers(this, requestsLimit, batchSize, startPage);
        restoreSummary.setEndTime();
        log.info(summaryUtils.createRestorerStatistics(restoreSummary));
    }

    @Override
    public void consumeLauDeletedUsers(List<DeletionLog> deletedUsers) {
        restoreSummary.increaseRequestsMade();
        restoreSummary.addProcessedNumber(deletedUsers.size());

        for (DeletionLog deletionLog : deletedUsers) {
            restoreService.restoreUser(deletionLog);
        }
    }
}
