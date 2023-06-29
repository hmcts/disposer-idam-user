package uk.gov.hmcts.reform.idam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
public class AuthClientConfiguration {

    @Bean
    @ConditionalOnProperty(name = "idam.s2s-auth.url")
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.name}") final String name,
        @Value("${idam.s2s-auth.secret}") final String secret,
        final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, name, serviceAuthorisationApi);
    }
}
