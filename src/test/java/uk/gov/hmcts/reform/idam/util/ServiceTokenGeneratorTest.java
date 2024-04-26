package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.exception.ServiceAuthTokenGenerationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ServiceTokenGeneratorTest {

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    ServiceTokenGenerator serviceTokenGenerator;

    @Test
    void shouldGetServiceToken() {
        String token = "serviceToken";
        when(authTokenGenerator.generate()).thenReturn(token);
        serviceTokenGenerator.generateServiceToken();
        assertThat(serviceTokenGenerator.getServiceAuthToken()).isEqualTo(token);
    }

    @Test
    void shouldThrowServiceAuthTokenGenerationException() {
        doThrow(new ServiceAuthTokenGenerationException("message"))
            .when(authTokenGenerator).generate();

        ServiceAuthTokenGenerationException thrown = assertThrows(
            ServiceAuthTokenGenerationException.class,
            serviceTokenGenerator::generateServiceToken
        );

        assertThat(thrown.getMessage()).contains("Unable to generate service auth token due to error -");
    }
}
