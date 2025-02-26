package uk.gov.hmcts.reform.idam.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.UserContent;
import uk.gov.hmcts.reform.idam.util.IdamTokenGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class StaleUsersService {

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    private boolean finished = false;
    private static final String PAGE_NUMBER_PARAM = "page";
    private static final String BATCH_SIZE_PARAM = "size";

    @Value("${stale-users.idam-start-page:0}")
    private int currentPage;
    private int totalStaleUsers;

    private final IdamClient client;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ParameterResolver parameterResolver;

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
            log.info("Page {} of {}", currentPage, staleUsersResponse.getTotalPages());
        } catch (Exception e) {
            log.error("StaleUsersService.getStaleUsers threw exception: {}", e.getMessage(), e);
            throw e;
        }

        finished = staleUsersResponse.getIsLast();
        currentPage += 1;
        totalStaleUsers += staleUsersResponse.getContent().size();

        String requiredRole = parameterResolver.getCitizenRole().toLowerCase();
        Set<String> rolesToDelete = parameterResolver
            .getAdditionalIdamCitizenRoles().orElse(new HashSet<>())
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        rolesToDelete.add(requiredRole);
        String filterPattern = parameterResolver.getCitizenRolesPattern();

        final List<UserContent> userContentList = staleUsersResponse
            .getContent()
            .stream()
            .filter(user -> user.getLowercasedRoles().contains(requiredRole))
            .filter(user -> rolesToDelete.containsAll(user.filterOutPatternRoles(filterPattern)))
            .toList();

        return userContentList.stream()
            .map(UserContent::getId)
            .toList();
    }

    public boolean hasFinished() {
        return finished;
    }
}
