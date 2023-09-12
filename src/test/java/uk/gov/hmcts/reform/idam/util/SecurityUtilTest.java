package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.exception.ServiceAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {
    @Mock
    private AuthTokenGenerator tokenGenerator;
    @Mock
    private ParameterResolver parameterResolver;
    @Mock
    private IdamClient idamClient;

    private static final String TOKEN = "123456789";
    private static final String ACCESS_TOKEN = "Bearer 1234";
    public static final String IDAM_TOKEN = "1234";
    public static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";
    public static final String SCOPE = "archive-user view-archived-user delete-archived-user";

    @InjectMocks
    private SecurityUtil securityUtil;

    @Test
    void shouldGetServiceAuthorization() {
        TokenResponse tokenResponse = new TokenResponse(
            IDAM_TOKEN,null,null,null,null,null);
        when(tokenGenerator.generate()).thenReturn(TOKEN);
        when(idamClient.getToken(null, null, null,
                                 CLIENT_CREDENTIALS_GRANT, null, null,
                                 SCOPE)).thenReturn(tokenResponse);
        securityUtil.generateTokens();

        assertThat(securityUtil.getServiceAuthorization()).isEqualTo(TOKEN);
        assertThat(securityUtil.getIdamClientToken()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void shouldThrowServiceAuthTokenGenerationException() {
        TokenResponse tokenResponse = new TokenResponse(
            IDAM_TOKEN,null,null,null,null,null);
        when(idamClient.getToken(null, null, null,
                                 CLIENT_CREDENTIALS_GRANT, null, null,
                                 SCOPE))
            .thenReturn(tokenResponse);
        doThrow(new ServiceAuthTokenGenerationException(TOKEN))
            .when(tokenGenerator).generate();

        ServiceAuthTokenGenerationException thrown = assertThrows(
            ServiceAuthTokenGenerationException.class,
            () -> securityUtil.generateTokens()
        );

        assertThat(thrown.getMessage()).contains("Unable to generate service auth token due to error -");
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {
        doThrow(new IdamAuthTokenGenerationException(TOKEN))
            .when(idamClient).getToken(null, null, null,
                                 CLIENT_CREDENTIALS_GRANT, null, null,
                                 SCOPE);

        IdamAuthTokenGenerationException thrown = assertThrows(
            IdamAuthTokenGenerationException.class,
            () -> securityUtil.generateTokens()
        );

        assertThat(thrown.getMessage()).contains("Unable to generate IDAM token due to error -");
    }

}
