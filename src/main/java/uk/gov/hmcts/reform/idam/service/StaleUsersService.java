package uk.gov.hmcts.reform.idam.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.exception.IdamApiException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.RestClient;
import uk.gov.hmcts.reform.idam.service.remote.responses.StaleUsersResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaleUsersService {

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    private boolean finished = false;
    private static final String PAGE_NUMBER_PARAM = "pageNumber";
    private int currentPage = 1;

    private final ParameterResolver idamConfig;
    private final RestClient client;

    public List<String> fetchStaleUsers() {
        final List<String> idamStaleUserIds = new LinkedList<>();
        boolean hasMore;

        final Response response = client.getRequest(
            idamConfig.getIdamHost(),
            idamConfig.getStaleUsersPath(),
            Map.of("Content-Type", "application/json"),
            Map.of(PAGE_NUMBER_PARAM, currentPage)
        );

        if (response.getStatus() == OK.value()) {
            var staleUsersResponse = response.readEntity(StaleUsersResponse.class);
            if (!staleUsersResponse.getStaleUsers().isEmpty()) {
                idamStaleUserIds.addAll(staleUsersResponse.getStaleUsers());
            }
            hasMore = staleUsersResponse.getMoreRecords() != null && staleUsersResponse.getMoreRecords();
            finished = !hasMore;
            currentPage += 1;
        } else {
            finished = true;
            log.error(String.format(
                "Stale users IDAM API call failed, status '%s', body: '%s'",
                response.getStatus(),
                response.readEntity(String.class)));
            throw new IdamApiException(
                String.format(
                    "Stale users IDAM API call failed, status '%s', body: '%s'",
                    response.getStatus(),
                    response.readEntity(String.class))
            );
        }

        return idamStaleUserIds;
    }

    public boolean hasFinished() {
        return finished;
    }
}
