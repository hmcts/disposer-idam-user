package uk.gov.hmcts.reform.idam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.requests.RestoreUserRequest;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.service.remote.responses.IdamCreateUserErrorResponse;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.idam.service.IdamUserRestorerService.MARKER;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreUserService {

    private final IdamClient idamClient;
    private final IdamTokenGenerator idamTokenGenerator;
    private final RestoreSummary restoreSummary;

    public void restoreUser(DeletionLog deletionLog) {
        try {
            RestoreUserRequest requestBody = createRequestBody(deletionLog);
            callApi(deletionLog.getUserId(), requestBody);
        } catch (IOException ioe) {
            log.error(
                "[{}] Failed to read idam response from restore user {}. Exception {}",
                MARKER,
                deletionLog.getUserId(),
                ioe.getMessage(),
                ioe
            );
        }
    }

    private void callApi(String userId, RestoreUserRequest requestBody) throws IOException {
        String authHeader = idamTokenGenerator.getIdamAuthorizationHeader();
        try (Response response = idamClient.restoreUser(authHeader, userId, requestBody)) {
            parsePotentialConflicts(response, userId);
        }
    }

    private void parsePotentialConflicts(Response response, String userId) throws IOException {
        HttpStatus status = HttpStatus.valueOf(response.status());

        if (status == HttpStatus.CREATED) {
            restoreSummary.addSuccess(userId);
        } else if (status == HttpStatus.CONFLICT) {
            String json = IOUtils.toString(response.body().asInputStream(), UTF_8);
            var errorResponse = new ObjectMapper().readValue(json, IdamCreateUserErrorResponse.class);
            String errorDescription = errorResponse.getErrorDescription();
            logPotentialErrorAndUpdateSummary(userId, errorDescription);
        } else {
            restoreSummary.addFailed(userId);
            log.info("[{}] Failed to reinstate the user due to http error {}: {}", MARKER, status, userId);
        }
    }

    private void logPotentialErrorAndUpdateSummary(final String userId, final String description) {
        if (!isEmpty(description)) {
            switch (description.toLowerCase()) {
                case "id in use":
                    log.info("[{}] User has been reinstated and reactivated the account: {}", MARKER, userId);
                    restoreSummary.addFailedToRestoreDueToReinstatedAndActiveAccount(userId);
                    break;
                case "email in use":
                    log.info("[{}] The user has created a new account: {}", MARKER, userId);
                    restoreSummary.addFailedToRestoreDueToNewAccountWithSameEmail(userId);
                    break;
                case "id already archived":
                    log.info("[{}] User has already been archived: {}", MARKER, userId);
                    restoreSummary.addFailedToRestoreDueToReinstatedAccount(userId);
                    break;
                case "email already archived":
                    log.info("[{}] The user has duplicate record stale users table in IdAM: {}", MARKER, userId);
                    restoreSummary.addFailedToRestoreDueToDuplicateEmail(userId);
                    break;
                default:
                    break;
            }
        }
    }

    private RestoreUserRequest createRequestBody(DeletionLog deletionLog) {
        return RestoreUserRequest.builder()
            .id(deletionLog.getUserId())
            .email(deletionLog.getEmailAddress())
            .firstName(deletionLog.getFirstName())
            .lastName(deletionLog.getLastName())
            .roles(List.of("citizen"))
            .build();
    }
}
