package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.exception.ServiceAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.exception.UserDetailsGenerationException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
    private static final String USER = "user@example.org";
    private static final String PASSWORD = "password";

    @InjectMocks
    private SecurityUtil securityUtil;

    @Test
    void shouldGetServiceAuthorization() {
        ReflectionTestUtils.setField(securityUtil, "parameterResolver", parameterResolver);
        final UserDetails userDetails = mock(UserDetails.class);

        when(tokenGenerator.generate()).thenReturn(TOKEN);
        when(parameterResolver.getIdamUsername()).thenReturn(USER);
        when(parameterResolver.getIdamPassword()).thenReturn(PASSWORD);
        when(idamClient.getAccessToken(USER, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(idamClient.getUserDetails(ACCESS_TOKEN)).thenReturn(userDetails);

        ReflectionTestUtils.invokeMethod(securityUtil, "generateTokens");

        assertThat(securityUtil.getServiceAuthorization()).isEqualTo(TOKEN);
        assertThat(securityUtil.getIdamClientToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(securityUtil.getUserDetails()).isEqualTo(userDetails);
    }

    @Test
    void shouldThrowServiceAuthTokenGenerationException() {

        ReflectionTestUtils.setField(securityUtil, "parameterResolver", parameterResolver);
        doThrow(new ServiceAuthTokenGenerationException(TOKEN))
            .when(tokenGenerator).generate();

        ServiceAuthTokenGenerationException thrown = assertThrows(
            ServiceAuthTokenGenerationException.class,
            () -> ReflectionTestUtils.invokeMethod(securityUtil, "generateTokens")
        );

        assertThat(thrown.getMessage())
            .contains("User disposer is unable to generate service auth token due to error -");

    }

    @Test
    void shouldThrowIdamAuthTokenGenerationException() {

        ReflectionTestUtils.setField(securityUtil, "parameterResolver", parameterResolver);

        when(parameterResolver.getIdamUsername()).thenReturn(USER);
        when(parameterResolver.getIdamPassword()).thenReturn(PASSWORD);

        doThrow(new IdamAuthTokenGenerationException(TOKEN))
            .when(idamClient).getAccessToken(USER, PASSWORD);

        IdamAuthTokenGenerationException thrown = assertThrows(
            IdamAuthTokenGenerationException.class,
            () -> ReflectionTestUtils.invokeMethod(securityUtil, "generateTokens")
        );

        assertThat(thrown.getMessage())
            .contains("User disposer is unable to generate IDAM token due to error -");
    }

    @Test
    void shouldThrowUserDetailsGenerationException() {

        ReflectionTestUtils.setField(securityUtil, "parameterResolver", parameterResolver);
        ReflectionTestUtils.setField(securityUtil, "idamClientToken", ACCESS_TOKEN);

        doThrow(new UserDetailsGenerationException(TOKEN))
            .when(idamClient).getUserDetails(ACCESS_TOKEN);

        var thrown = assertThrows(
            UserDetailsGenerationException.class,
            () -> ReflectionTestUtils.invokeMethod(securityUtil, "generateUserDetails")
        );

        assertThat(thrown.getMessage())
            .contains("User disposer is unable to generate UserDetails due to error -");

    }


}
