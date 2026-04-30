package uk.gov.hmcts.reform.idam.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CcdPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(CcdPropertiesConfiguration.class);

    @Test
    void shouldBindCcdProperties() {
        contextRunner
            .withPropertyValues(
                "ccd.role-assignment.host=http://localhost:5000",
                "ccd.role-assignment.batch-size=75",
                "ccd.role-assignment.request-page-size=1000"
            )
            .run(context -> {
                final CcdProperties properties = context.getBean(CcdProperties.class);

                assertThat(properties.getRoleAssignment().getHost()).isEqualTo("http://localhost:5000");
                assertThat(properties.getRoleAssignment().getBatchSize()).isEqualTo(75);
                assertThat(properties.getRoleAssignment().getRequestPageSize()).isEqualTo(1000);
            });
    }

    @EnableConfigurationProperties(CcdProperties.class)
    private static final class CcdPropertiesConfiguration {
    }
}
