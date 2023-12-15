package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.client.LauIdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.idam.service.IdamUserRestorerService.MARKER;

@Service
@RequiredArgsConstructor
@Slf4j
public class LauIdamUserService {

    private final LauIdamClient lauClient;
    private final SecurityUtil securityUtil;

    private boolean hasMoreRecords = true;
    private int backOffInSeconds = 1;

    @Value("${lau.api.max_backoff_wait}")
    private long maxBackoffInSecondsThenGiveUp = 32L;

    private final List<Integer> retryableStatuses = List.of(403, 502, 504);
    private static final long ONE_SECOND_IN_MS = 1000;

    public void retrieveDeletedUsers(
        LauDeletedUsersConsumer consumer,
        int requestsLimit,
        int batchSize,
        int startPage
    ) {
        int requestsMade = 0;
        List<DeletionLog> deletedUsers = List.of();
        boolean proceed = true;

        while (proceed && hasMoreRecords && requestsMade < requestsLimit) {
            int currentPage = startPage + requestsMade;

            try {
                deletedUsers = fetchDeletedUsers(batchSize, currentPage);
                backOffInSeconds = 1;
                requestsMade++;
            } catch (final FeignException fe) {
                log.error("[{}] Exception fetching deleted users {}", MARKER, fe.getMessage());
                proceed = retry(fe);
            }

            log.info(
                "[Page {}] Fetched deleted users {}",
                currentPage,
                deletedUsers.stream().map(DeletionLog::getUserId).toList()
            );

            consumer.consumeLauDeletedUsers(deletedUsers);
        }

        if (hasMoreRecords) {
            log.warn("Stopped fetching deleted users even though not all are retrieved");
        }
    }

    private List<DeletionLog> fetchDeletedUsers(final int batchSize, final int page) {
        Map<String, String> authHeaders = securityUtil.getAuthHeaders();
        final DeletedUsersResponse response = lauClient.getDeletedUsers(authHeaders, batchSize, page);
        hasMoreRecords = response.isMoreRecords();
        return response.getDeletionLogs();
    }

    private boolean retry(FeignException fe) {
        if (fe.status() == 403) {
            securityUtil.generateTokens();
        }

        if (retryableStatuses.contains(fe.status())) {
            coolOff();
            return backOffInSeconds <= maxBackoffInSecondsThenGiveUp;
        }
        return false;
    }

    private void coolOff() {
        try {
            Thread.sleep(backOffInSeconds * ONE_SECOND_IN_MS);
            backOffInSeconds = backOffInSeconds * 2;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
