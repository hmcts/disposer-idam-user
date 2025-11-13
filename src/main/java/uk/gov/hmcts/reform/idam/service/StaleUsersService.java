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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class StaleUsersService implements Iterator<List<String>> {

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    private boolean finished = false;
    private static final String PREVIOUS_USER_PARAM = "previousUserId";
    private static final String BATCH_SIZE_PARAM = "size";

    @Value("${stale-users.idam-start-page:0}")
    private int currentPage;
    private int totalStaleUsers;

    private String previousUserId;
    private UserContent pendingAnchor;

    private final IdamClient client;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ParameterResolver parameterResolver;

    private List<String> fetchNextBatch() {
        final StaleUsersResponse staleUsersResponse;
        Map<String, Object> query = new ConcurrentHashMap<>();
        query.put(BATCH_SIZE_PARAM, parameterResolver.getStaleUsersBatchSize() + 1);
        if (pendingAnchor != null) {
            query.put(PREVIOUS_USER_PARAM, pendingAnchor.getId());
        }

        staleUsersResponse = client.getStaleUsers(idamTokenGenerator.getIdamAuthorizationHeader(), query);
        log.debug(
            "Returned stale user IDs: {}",
            staleUsersResponse.content().stream().map(UserContent::getId).toList()
        );

        if (staleUsersResponse.content().isEmpty()) {
            finished = true;
            return pendingAnchor == null ? List.of() : filterByRoles(List.of(pendingAnchor));
        }

        UserContent newAnchorToKeep = staleUsersResponse.content().removeLast();
        log.debug("Fetched page {}, next anchor: {}", currentPage++, newAnchorToKeep);

        List<UserContent> users = new ArrayList<>();

        if (pendingAnchor != null) {
            users.add(pendingAnchor);
        }
        users.addAll(staleUsersResponse.content());
        finished = staleUsersResponse.isLast();
        if (finished) {
            users.add(newAnchorToKeep);
        }
        pendingAnchor = newAnchorToKeep;

        totalStaleUsers += staleUsersResponse.content().size();

        return filterByRoles(users);
    }

    private List<String> filterByRoles(List<UserContent> users) {
        String requiredRole = parameterResolver.getCitizenRole().toLowerCase();
        Set<String> rolesToDelete = parameterResolver
            .getAdditionalIdamCitizenRoles().orElse(new HashSet<>())
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        rolesToDelete.add(requiredRole);
        String filterPattern = parameterResolver.getCitizenRolesPattern();

        final List<UserContent> userContentList = users
            .stream()
            .filter(user -> user.getLowercasedRoles().contains(requiredRole))
            .filter(user -> rolesToDelete.containsAll(user.filterOutPatternRoles(filterPattern)))
            .toList();

        return userContentList.stream()
            .map(UserContent::getId)
            .toList();
    }

    @Override
    public boolean hasNext() {
        return !finished;
    }

    @Override
    public List<String> next() {
        return fetchNextBatch();
    }
}
