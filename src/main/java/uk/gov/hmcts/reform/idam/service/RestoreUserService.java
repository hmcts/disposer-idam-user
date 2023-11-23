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

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.reform.idam.service.IdamUserRestorerService.MARKER;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreUserService {

    private final IdamClient idamClient;
    private final IdamTokenGenerator idamTokenGenerator;

    public boolean restoreUser(DeletionLog deletionLog) {

        try {
            RestoreUserRequest requestBody = createRequestBody(deletionLog);
            HttpStatus status = callApi(deletionLog.getUserId(), requestBody);
            if (status == HttpStatus.CREATED) {
                return true;
            } else {
                log.error(
                    "[{}] Failed to restore user from deletion log {}. HTTP Status {}",
                    MARKER,
                    deletionLog.getUserId(),
                    status
                );
            }

        } catch (IOException ioe) {
            log.error(
                "[{}] Failed to read idam response from restore user {}. Exception {}",
                MARKER,
                deletionLog.getUserId(),
                ioe.getMessage(),
                ioe
            );
        }
        return false;
    }

    private HttpStatus callApi(String userId, RestoreUserRequest requestBody) throws IOException {
        String authHeader = idamTokenGenerator.getIdamAuthorizationHeader();
        try (Response response = idamClient.restoreUser(authHeader, userId, requestBody)) {
            return handleStatus(response, userId);
        }
    }

    private HttpStatus handleStatus(Response response, String userId) throws IOException {
        HttpStatus status = HttpStatus.valueOf(response.status());
        if (status == HttpStatus.CONFLICT) {
            status = parseConflict(response.body(), userId);
        }
        return status;
    }

    private HttpStatus parseConflict(Response.Body body, String userId) throws IOException {
        String json = IOUtils.toString(body.asInputStream(), UTF_8);
        var errorResponse = new ObjectMapper().readValue(json, IdamCreateUserErrorResponse.class);
        String description = errorResponse.getErrorDescription();
        if ("id in use".equalsIgnoreCase(description) || "id already archived".equalsIgnoreCase(description)) {
            log.info("[{}] User already exists with id {}", MARKER, userId);
            return HttpStatus.CREATED;
        }
        return HttpStatus.CONFLICT;
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
