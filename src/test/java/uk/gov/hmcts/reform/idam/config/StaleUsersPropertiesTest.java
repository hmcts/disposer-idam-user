package uk.gov.hmcts.reform.idam.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class StaleUsersPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(StaleUsersPropertiesConfiguration.class);

    @Test
    void shouldBindStaleUsersProperties() {
        contextRunner
            .withPropertyValues(
                "stale-users.batch-size=100",
                "stale-users.requests.limit=10",
                "stale-users.citizen.mandatory-role=disposer-test",
                "stale-users.citizen.roles=role1,role2",
                "stale-users.citizen.letter-role-pattern=pattern",
                "stale-users.simulation=true",
                "stale-users.cut-off-time=06:00"
            )
            .run(context -> {
                final StaleUsersProperties properties = context.getBean(StaleUsersProperties.class);

                assertThat(properties.getBatchSize()).isEqualTo(100);
                assertThat(properties.getRequests().getLimit()).isEqualTo(10);
                assertThat(properties.getCitizen().getMandatoryRole()).isEqualTo("disposer-test");
                assertThat(properties.getCitizen().getRoles()).containsExactly("role1", "role2");
                assertThat(properties.getCitizen().getLetterRolePattern()).isEqualTo("pattern");
                assertThat(properties.isSimulation()).isTrue();
                assertThat(properties.getCutOffTime()).isEqualTo(LocalTime.of(6, 0));
            });
    }

    @Test
    void shouldApplyStaleUsersDefaultsWhenOptionalPropertiesAreMissing() {
        contextRunner.run(context -> {
            final StaleUsersProperties properties = context.getBean(StaleUsersProperties.class);

            assertThat(properties.getCitizen().getMandatoryRole()).isNull();
            assertThat(properties.getCitizen().getRoles()).isEmpty();
            assertThat(properties.getCitizen().getLetterRolePattern()).isNull();
        });
    }

    @EnableConfigurationProperties(StaleUsersProperties.class)
    private static final class StaleUsersPropertiesConfiguration {
    }
}
