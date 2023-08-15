package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.service.remote.IdamClient;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
public class DeleteUserService {

    private final IdamClient idamClient;

    public DeleteUserService(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    public void deleteUsers(List<String> batchStaleUserIds) {
        for (String userId : batchStaleUserIds) {
            deleteUser(userId);
        }
    }

    private void deleteUser(String userId) {
        Response response = idamClient.deleteUser(userId);

        if (response.status() != NO_CONTENT.value()) {
            String msg = String.format("User with id '%s' deletion failed", userId);
            log.error(msg);
            throw new IdamApiException(msg);
        }
    }
}