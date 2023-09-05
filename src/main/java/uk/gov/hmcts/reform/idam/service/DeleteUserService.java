package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
public class DeleteUserService {

    private ParameterResolver parameterResolver;

    private final IdamClient idamClient;

    public DeleteUserService(IdamClient idamClient, ParameterResolver parameterResolver) {
        this.idamClient = idamClient;
        this.parameterResolver = parameterResolver;
    }

    public void deleteUsers(List<String> batchStaleUserIds) {

        log.info("s2s Secrets",parameterResolver.getS2sSecret());
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
