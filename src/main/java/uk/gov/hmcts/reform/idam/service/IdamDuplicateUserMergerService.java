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

    public static final String MARKER = "USER_ROLE_MERGER";

    @Value("${duplicate-user-merger.requests.limit}")
    private int requestsLimit;

    @Value("${duplicate-user-merger.batch.size}")
    private int batchSize;

    @Value("${duplicate-user-merger.start.page}")
    private int startPage;

    @Value("${duplicate-user-merger.dry_run}")
    private boolean dryRunMode;

    private final LauIdamUserService lauService;
    private final DuplicateUserSummary duplicateUserSummary;
    private final IdamTokenGenerator idamTokenGenerator;
    private final IdamClient idamClient;
    private final UserRoleMergeService userRoleMergeService;
    private final LoggingSummaryUtils summaryUtils;

    public void run() {
        duplicateUserSummary.setIsDryRunMode(dryRunMode);
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
                checkIfUserIdsOnEmailMatch(response.get(0), deletionLog);
            }
        } catch (FeignException fe) {
            log.error(fe.getMessage(), fe);
        }
    }

    private void checkIfUserIdsOnEmailMatch(IdamQueryResponse queryResponse, DeletionLog deletionLog) {
        final boolean emailsMatch = queryResponse.getEmail().strip()
            .equalsIgnoreCase(deletionLog.getEmailAddress().strip());
        final boolean userIdsMatch = queryResponse.getId().equalsIgnoreCase(deletionLog.getUserId());

        if (userIdsMatch && emailsMatch) {
            duplicateUserSummary.increaseMatchedIds();
        }

        if (emailsMatch && !userIdsMatch) {
            log.warn(
                "[{}] Ids do not match, deleted {}, existing {}",
                MARKER,
                deletionLog.getUserId(),
                queryResponse.getId()
            );

            duplicateUserSummary.increaseEmailMultipleIds();
            userRoleMergeService.mergeRoleAssignments(deletionLog.getUserId(), queryResponse.getId());
        }
    }
}
