package uk.gov.hmcts.reform.idam.parameter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParameterChecker {

    @Value("${service.enabled}")
    private boolean isDisposerEnabled;

    @Value("${service.restorer_enabled}")
    private boolean isRestorerEnabled;

    @PostConstruct
    public void init() {
        if (isRestorerEnabled && isDisposerEnabled) {
            throw new IllegalStateException("Deletion and restorer are both enabled, please choose only one.");
        }
    }
}
