package uk.gov.hmcts.reform.idam.service.remote;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class StaleUsersRequestParams {
    private Map<String, Object> params = new ConcurrentHashMap<>();
}
