package uk.gov.hmcts.reform.idam.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.Set;

@ConfigurationProperties(prefix = "stale-users")
@Getter
@Setter
public class StaleUsersProperties {

    private int batchSize;
    private Requests requests = new Requests();
    private Citizen citizen = new Citizen();
    private boolean simulation;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime cutOffTime;

    private String idamSortDirection = "ASC";

    @Getter
    @Setter
    public static class Requests {
        private int limit;
    }

    @Getter
    @Setter
    public static class Citizen {
        private String mandatoryRole;
        private Set<String> roles = Set.of();
        private String letterRolePattern;
    }
}
