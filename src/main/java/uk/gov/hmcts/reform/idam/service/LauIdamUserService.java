package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.service.remote.client.LauIdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.idam.util.ServiceTokenGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.idam.service.IdamUserRestorerService.MARKER;

@Service
@RequiredArgsConstructor
@Slf4j
public class LauIdamUserService {

    private final LauIdamClient lauClient;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ServiceTokenGenerator serviceTokenGenerator;

    private boolean more = true;

    public List<DeletionLog> fetchDeletedUsers(final int batchSize) {
        try {
            final DeletedUsersResponse response = lauClient.getDeletedUsers(getAuthHeaders(), batchSize);
            more = response.isMoreRecords();
            return response.getDeletionLogs();
        } catch (FeignException fe) {
            log.error("[{}] Exception fetching deleted users {}", MARKER, fe.getMessage(), fe);
            more = false;
        }

        return List.of();
    }

    public boolean deleteLogEntry(String userId) {
        HttpStatus status = callApi(userId);
        if (status != HttpStatus.NO_CONTENT) {
            log.error(
                "[{}] Unexpected status code from user deletion log deletion for user {}. Http Status: {}",
                MARKER,
                userId,
                status
            );
        }
        return status == HttpStatus.NO_CONTENT;
    }

    public boolean hasMore() {
        return more;
    }

    private Map<String, String> getAuthHeaders() {
        return Map.of(
            "Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader(),
            "ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken()
        );
    }

    private HttpStatus callApi(String userId) {
        try (Response response = lauClient.deleteLogEntry(getAuthHeaders(), userId)) {
            return HttpStatus.valueOf(response.status());
        }
    }
}
