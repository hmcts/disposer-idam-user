package uk.gov.hmcts.reform.idam.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${restorer.batch.size}")
    private int batchSize;
    private int page = 1;
    private boolean hasMoreRecords = true;

    public List<DeletionLog> fetchDeletedUsers() {
        try {
            final DeletedUsersResponse response = lauClient.getDeletedUsers(getAuthHeaders(), batchSize, page++);
            hasMoreRecords = response.isMoreRecords();
            return response.getDeletionLogs();
        } catch (final FeignException feignException) {
            log.error("[{}] Exception fetching deleted users {}", MARKER, feignException.getMessage(), feignException);
            hasMoreRecords = false;
        }
        return List.of();
    }

    public boolean hasMoreRecords() {
        return hasMoreRecords;
    }

    private Map<String, String> getAuthHeaders() {
        return Map.of(
            "Authorization", idamTokenGenerator.getPasswordTypeAuthorizationHeader(),
            "ServiceAuthorization", serviceTokenGenerator.getServiceAuthToken()
        );
    }
}
