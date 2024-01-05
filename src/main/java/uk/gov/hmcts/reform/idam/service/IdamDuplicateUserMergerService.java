package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.service.remote.responses.IdamQueryResponse;
import uk.gov.hmcts.reform.idam.util.DuplicateUserSummary;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.LoggingSummaryUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.idam.util.Constants.QUERY_PARAM;
import static uk.gov.hmcts.reform.idam.util.Constants.QUERY_SIZE;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamDuplicateUserMergerService implements LauDeletedUsersConsumer {

    @Value("${duplicate-user-logger.requests.limit}")
    private int requestsLimit;

    @Value("${duplicate-user-logger.batch.size}")
    private int batchSize;

    @Value("${duplicate-user-logger.start.page}")
    private int startPage;

    private final LauIdamUserService lauService;
    private final DuplicateUserSummary duplicateUserSummary;
    private final IdamTokenGenerator idamTokenGenerator;
    private final IdamClient idamClient;
    private final UserRoleService userRoleService;
    private final LoggingSummaryUtils summaryUtils;

    public void run() {
        duplicateUserSummary.setStartTime();
        lauService.retrieveDeletedUsers(this, requestsLimit, batchSize, startPage);
        duplicateUserSummary.setEndTime();
        log.info(summaryUtils.createMergerStatistics(duplicateUserSummary));
    }

    @Override
    public void consumeLauDeletedUsers(List<DeletionLog> deletedUsers) {
        for (DeletionLog deletionLog : deletedUsers) {
            processDeletedUser(deletionLog);
        }
    }

    private void processDeletedUser(DeletionLog deletionLog) {
        try {
            final List<IdamQueryResponse> response = idamClient.queryUser(
                idamTokenGenerator.getIdamAuthorizationHeader(),
                Map.of(
                    QUERY_PARAM, "email:" + deletionLog.getEmailAddress().trim(),
                    QUERY_SIZE, "1000"
                )
            );
            if (response.isEmpty()) {
                duplicateUserSummary.increaseNoMatches();
            } else if (response.size() > 1) {
                duplicateUserSummary.increaseMultipleMatches();
            } else {
                checkIfMatches(response.get(0), deletionLog);
            }
        } catch (FeignException fe) {
            log.error(fe.getMessage(), fe);
            //TODO: do something meaningful
        }
    }

    private void checkIfMatches(IdamQueryResponse queryResponse, DeletionLog deletionLog) {
        if (queryResponse.getId().equalsIgnoreCase(deletionLog.getUserId())) {
            log.info("We have a match, nothing to see here");
            duplicateUserSummary.increaseMatchedIds();
        } else {
            log.warn("Ids do not match, deleted {}, existing {}", deletionLog.getUserId(), queryResponse.getId());
            duplicateUserSummary.increaseEmailMultipleIds();
            userRoleService.mergeRoleAssignments(deletionLog.getUserId(), queryResponse.getId());
        }
    }
}
