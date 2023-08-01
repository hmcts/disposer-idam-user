package uk.gov.hmcts.reform.idam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.IdamClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaleUsersService {

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    private boolean finished = false;
    private static final String PAGE_NUMBER_PARAM = "pageNumber";
    private static final String BATCH_SIZE_PARAM = "size";
    private int currentPage = 1;

    private final IdamClient client;
    private final ParameterResolver idamConfig;

    public List<String> fetchStaleUsers() {
        boolean hasMore;
        StaleUsersResponse staleUsersResponse = client.getStaleUsers(
            Map.of(
                PAGE_NUMBER_PARAM, currentPage,
                BATCH_SIZE_PARAM, idamConfig.getBatchSize()
            )
        );

        hasMore = staleUsersResponse.getMoreRecords() != null && staleUsersResponse.getMoreRecords();
        finished = !hasMore;
        currentPage += 1;

        return staleUsersResponse.getStaleUsers();
    }

    public boolean hasFinished() {
        return finished;
    }
}
