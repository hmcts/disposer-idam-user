package uk.gov.hmcts.reform.idam.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ccd")
@Getter
@Setter
public class CcdProperties {

    private RoleAssignment roleAssignment = new RoleAssignment();

    @Getter
    @Setter
    public static class RoleAssignment {
        private String host;
        private int batchSize;
        private int requestPageSize;
    }
}
