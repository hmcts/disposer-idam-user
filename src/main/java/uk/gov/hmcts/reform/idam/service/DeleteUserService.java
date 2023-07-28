package uk.gov.hmcts.reform.idam.service;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.RestClient;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
public class DeleteUserService {

    private final ParameterResolver idamConfig;
    private final RestClient client;

    public DeleteUserService(RestClient client, ParameterResolver idamConfig) {
        this.client = client;
        this.idamConfig = idamConfig;
    }

    public void deleteUsers(List<String> batchStaleUserIds) {
        for (String userId: batchStaleUserIds) {
            deleteUser(userId);
        }
    }

    private void deleteUser(String userId) {
        String path = idamConfig.getDeleteUserPath() + userId;
        Response response = client.deleteRequest(idamConfig.getIdamHost(), path);

        if (response.getStatus() != NO_CONTENT.value()) {
            String msg = String.format("User with id '%s' deletion failed", userId);
            log.error(msg);
            // it might not be a good idea to throw an exception here
            // without handling, as it would stop the whole application
            // throw new IdamApiException(msg);
        }
    }
}
