package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.idam.util.IdamTokenGenerator.CLIENT_CREDENTIALS_GRANT;
import static uk.gov.hmcts.reform.idam.util.IdamTokenGenerator.SCOPE;

@ExtendWith(MockitoExtension.class)
class IdamTokenGeneratorTest {
    @Mock
    IdamClient idamClient;

    @Mock
    ParameterResolver parameterResolver;

    @InjectMocks
    IdamTokenGenerator idamTokenGenerator;

    @BeforeEach
    void setUp() {
        when(parameterResolver.getClientId()).thenReturn("ClientId");
        when(parameterResolver.getClientSecret()).thenReturn("Client secret");
    }

    @Test
    void shouldGetIdamToken() {
        String token = "accessToken";
        TokenResponse tokenResponse = new TokenResponse(
            token,
            null,
            null,
            null,
            null,
            null
        );
        when(idamClient.getToken(
            "ClientId",
            "Client secret",
            null,
            CLIENT_CREDENTIALS_GRANT,
            null,
            null,
            SCOPE
        )).thenReturn(tokenResponse);
        idamTokenGenerator.generateIdamToken();
        assertThat(idamTokenGenerator.getIdamClientToken()).isEqualTo(token);
    }

    @Test
    void shouldGetTokenPrefixedWithBearer() {
        String token = "accessToken";
        TokenResponse tokenResponse = new TokenResponse(
            token,
            null,
            null,
            null,
            null,
            null
        );
        when(idamClient.getToken(
            "ClientId",
            "Client secret",
            null,
            CLIENT_CREDENTIALS_GRANT,
            null,
            null,
            SCOPE
        )).thenReturn(tokenResponse);
        idamTokenGenerator.generateIdamToken();
        assertThat(idamTokenGenerator.getIdamAuthorizationHeader()).isEqualTo("Bearer " + token);
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {
        doThrow(new IdamAuthTokenGenerationException("message")).when(idamClient).getToken(
            "ClientId",
            "Client secret",
            null,
            CLIENT_CREDENTIALS_GRANT,
            null,
            null,
            SCOPE
        );
        IdamAuthTokenGenerationException thrown = assertThrows(
            IdamAuthTokenGenerationException.class,
            () -> idamTokenGenerator.generateIdamToken()
        );

        assertThat(thrown.getMessage()).contains("Unable to generate IDAM token due to error -");
    }

}
