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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.idam.util.IdamTokenGenerator.IDAM_GRANT_TYPE;
import static uk.gov.hmcts.reform.idam.util.IdamTokenGenerator.IDAM_SCOPE;
import static uk.gov.hmcts.reform.idam.util.IdamTokenGenerator.PASSWORD_GRANT_TYPE;
import static uk.gov.hmcts.reform.idam.util.IdamTokenGenerator.ROLE_ASSIGNMENT_SCOPE;

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
        when(parameterResolver.getClientUserName()).thenReturn("username");
        when(parameterResolver.getClientPassword()).thenReturn("password");
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
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                null,
                IDAM_GRANT_TYPE,
                parameterResolver.getClientUserName(),
                parameterResolver.getClientPassword(),
                IDAM_SCOPE
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
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                null,
                IDAM_GRANT_TYPE,
                parameterResolver.getClientUserName(),
                parameterResolver.getClientPassword(),
                IDAM_SCOPE
        )).thenReturn(tokenResponse);
        idamTokenGenerator.generateIdamToken();
        assertThat(idamTokenGenerator.getIdamAuthorizationHeader()).isEqualTo("Bearer " + token);
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {
        when(idamClient.getToken(
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                null,
                IDAM_GRANT_TYPE,
                parameterResolver.getClientUserName(),
                parameterResolver.getClientPassword(),
                IDAM_SCOPE
        )).thenThrow(new IdamAuthTokenGenerationException("message"));

        IdamAuthTokenGenerationException thrown = assertThrows(
                IdamAuthTokenGenerationException.class,
                idamTokenGenerator::generateIdamToken
        );

        assertThat(thrown.getMessage()).contains("Unable to generate IDAM token due to error -");
    }

    @Test
    void shouldGetRoleAssignmentIdamToken() {
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
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                null,
                PASSWORD_GRANT_TYPE,
                parameterResolver.getClientUserName(),
                parameterResolver.getClientPassword(),
                ROLE_ASSIGNMENT_SCOPE
        )).thenReturn(tokenResponse);
        idamTokenGenerator.generatePasswordTypeToken();
        assertThat(idamTokenGenerator.getPasswordTypeClientToken()).isEqualTo(token);
    }

    @Test
    void shouldGetRoleAssignmentsTokenPrefixedWithBearer() {
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
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                null,
                PASSWORD_GRANT_TYPE,
                parameterResolver.getClientUserName(),
                parameterResolver.getClientPassword(),
                ROLE_ASSIGNMENT_SCOPE
        )).thenReturn(tokenResponse);
        idamTokenGenerator.generatePasswordTypeToken();
        assertThat(idamTokenGenerator.getPasswordTypeAuthorizationHeader()).isEqualTo("Bearer " + token);
    }

    @Test
    void shouldThrowIdamAuthTokenGenerationForRoleAssignmentsException() {
        when(idamClient.getToken(
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                null,
                PASSWORD_GRANT_TYPE,
                parameterResolver.getClientUserName(),
                parameterResolver.getClientPassword(),
                ROLE_ASSIGNMENT_SCOPE
        )).thenThrow(new IdamAuthTokenGenerationException("message"));

        IdamAuthTokenGenerationException thrown = assertThrows(
                IdamAuthTokenGenerationException.class,
                idamTokenGenerator::generatePasswordTypeToken
        );

        assertThat(thrown.getMessage()).contains("Unable to generate Role Assignment IDAM token due to error -");
    }
}
