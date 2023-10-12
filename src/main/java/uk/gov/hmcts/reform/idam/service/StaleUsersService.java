package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.aop.Retry;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.UserContent;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaleUsersService {

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    private boolean finished = false;
    private static final String PAGE_NUMBER_PARAM = "page";
    private static final String BATCH_SIZE_PARAM = "size";
    private int currentPage;

    private final IdamClient client;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ParameterResolver parameterResolver;

    @Retry(retryAttempts = 2)
    public List<String> fetchStaleUsers() {
        final StaleUsersResponse staleUsersResponse;
        try {
            staleUsersResponse = client.getStaleUsers(
                idamTokenGenerator.getIdamAuthorizationHeader(),
                Map.of(
                    PAGE_NUMBER_PARAM, currentPage,
                    BATCH_SIZE_PARAM, parameterResolver.getBatchSize()
                )
            );
        } catch (Exception e) {
            log.error("StaleUsersService.getStaleUsers threw exception: {}", e.getMessage(), e);
            throw e;
        }

        finished = staleUsersResponse.getIsLast();
        currentPage += 1;

        String roleToDelete = parameterResolver.getIdamRoleToDelete();

        return staleUsersResponse
                .getContent()
                .stream()
                .filter(user -> user.getRoles() != null
                    && user.getRoles().size() == 1
                    && user.getRoles().get(0).toLowerCase().equalsIgnoreCase(roleToDelete))
                .map(UserContent::getId)
                .toList();
    }

    public boolean hasFinished() {
        return finished;
    }
}
