package uk.gov.hmcts.reform.idam.service;

import feign.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Service
@Slf4j
@AllArgsConstructor
public class DeleteUserService {

    private final IdamClient idamClient;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ParameterResolver parameterResolver;


    public void deleteUsers(List<String> batchStaleUserIds) {
        if (!parameterResolver.getIsSimulation()) {
            for (String userId : batchStaleUserIds) {
                deleteUser(userId);
            }
        }
    }

    public void deleteUser(String userId) {
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

        if (response.status() != OK.value()) {
            String msg = String.format(
                "User with id '%s' deletion failed (response status %s)",
                userId,
                response.status()
            );
            log.error(msg);
            throw new IdamApiException(msg);
        }
    }
}
