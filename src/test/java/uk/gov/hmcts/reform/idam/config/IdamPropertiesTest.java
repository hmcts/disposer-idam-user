package uk.gov.hmcts.reform.idam.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class IdamPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(IdamPropertiesConfiguration.class);

    @Test
    void shouldBindIdamProperties() {
        contextRunner
            .withPropertyValues(
                "idam.api.port=5000",
                "idam.api.url=http://localhost:5000",
                "idam.s2s-auth.name=disposer-idam-user",
                "idam.s2s-auth.url=http://localhost:4502",
                "idam.s2s-auth.secret=s2s-secret",
                "idam.client.id=client-id",
                "idam.client.secret=client-secret",
                "idam.client.username=user@example.org",
                "idam.client.password=client-password",
                "idam.client.redirect_uri=https://disposer-idam-user/oauth2/callback"
            )
            .run(context -> {
                final IdamProperties properties = context.getBean(IdamProperties.class);

                assertThat(properties.getApi().getPort()).isEqualTo(5000);
                assertThat(properties.getApi().getUrl()).isEqualTo("http://localhost:5000");
                assertThat(properties.getS2sAuth().getName()).isEqualTo("disposer-idam-user");
                assertThat(properties.getS2sAuth().getUrl()).isEqualTo("http://localhost:4502");
                assertThat(properties.getS2sAuth().getSecret()).isEqualTo("s2s-secret");
                assertThat(properties.getClient().getId()).isEqualTo("client-id");
                assertThat(properties.getClient().getSecret()).isEqualTo("client-secret");
                assertThat(properties.getClient().getUsername()).isEqualTo("user@example.org");
                assertThat(properties.getClient().getPassword()).isEqualTo("client-password");
                assertThat(properties.getClient().getRedirectUri())
                    .isEqualTo("https://disposer-idam-user/oauth2/callback");
            });
    }

    @EnableConfigurationProperties(IdamProperties.class)
    private static final class IdamPropertiesConfiguration {
    }
}
