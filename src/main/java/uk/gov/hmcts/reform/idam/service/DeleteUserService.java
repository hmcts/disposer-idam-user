package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
public class DeleteUserService {

    private final IdamClient idamClient;
    private final IdamTokenGenerator idamTokenGenerator;

    public DeleteUserService(IdamClient idamClient, IdamTokenGenerator idamTokenGenerator) {
        this.idamClient = idamClient;
        this.idamTokenGenerator = idamTokenGenerator;
    }

    public void deleteUsers(List<String> batchStaleUserIds) {
        for (String userId : batchStaleUserIds) {
            deleteUser(userId);
        }
    }

    private void deleteUser(String userId) {
        final Response response;
        try {
            response = idamClient.deleteUser(
                idamTokenGenerator.getIdamAuthorizationHeader(),
                userId
            );
        } catch (Exception e) {
            log.error("DeleteUserService.deleteUser threw exception: {}", e.getMessage(), e);
            throw e;
        }

        if (response.status() != NO_CONTENT.value()) {
            String msg = String.format("User with id '%s' deletion failed", userId);
            log.error(msg);
            throw new IdamApiException(msg);
        }
    }
}
