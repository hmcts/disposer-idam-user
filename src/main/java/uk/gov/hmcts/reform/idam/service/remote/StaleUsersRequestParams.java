package uk.gov.hmcts.reform.idam.service.remote;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class StaleUsersRequestParams {

    @Value("${rest-client.stale-users-size}")
    private int staleUsersAmount = 100;

    private Map<String, Object> params = new ConcurrentHashMap<>();
}
