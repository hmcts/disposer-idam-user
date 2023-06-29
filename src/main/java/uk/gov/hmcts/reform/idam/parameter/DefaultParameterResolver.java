package uk.gov.hmcts.reform.idam.parameter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultParameterResolver implements ParameterResolver {

    @Value("${idam.api.url}")
    private String idamHost;

    @Value("${idam.api.username}")
    private String idamApiUsername;

    @Value("${idam.api.password}")
    private String idamApiPassword;

    @Override
    public String getIdamHost() {
        return idamHost;
    }

    @Override
    public String getIdamUsername() {
        return idamApiUsername;
    }

    @Override
    public String getIdamPassword() {
        return idamApiPassword;
    }
}
