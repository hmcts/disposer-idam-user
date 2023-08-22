package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.exception.ServiceAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

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

    @InjectMocks
    private SecurityUtil securityUtil;

    @Test
    void shouldGetServiceAuthorization() {
        ReflectionTestUtils.setField(securityUtil, "parameterResolver", parameterResolver);

        when(tokenGenerator.generate()).thenReturn(TOKEN);
        when(idamClient.getAccessToken(null, null)).thenReturn("Bearer 1234");


        ReflectionTestUtils.invokeMethod(securityUtil, "generateTokens");

        assertThat(securityUtil.getServiceAuthorization()).isEqualTo(TOKEN);
        assertThat(securityUtil.getIdamClientToken()).isEqualTo(ACCESS_TOKEN);
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

        doThrow(new IdamAuthTokenGenerationException(TOKEN))
            .when(idamClient).getAccessToken(null, null);

        IdamAuthTokenGenerationException thrown = assertThrows(
            IdamAuthTokenGenerationException.class,
            () -> ReflectionTestUtils.invokeMethod(securityUtil, "generateTokens")
        );

        assertThat(thrown.getMessage())
            .contains("User disposer is unable to generate IDAM token due to error -");
    }

}
