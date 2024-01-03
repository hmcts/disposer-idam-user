package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {
    @Mock
    IdamTokenGenerator idamTokenGenerator;

    @Mock
    ServiceTokenGenerator serviceTokenGenerator;

    @InjectMocks
    SecurityUtil securityUtil;

    @Test
    void shouldCallGenerators() {
        clearInvocations(idamTokenGenerator, serviceTokenGenerator);
        securityUtil.generateTokens();
        verify(idamTokenGenerator, times(1)).generateIdamToken();
        verify(serviceTokenGenerator, times(1)).generateServiceToken();
    }

    @Test
    void authHeadersGetShouldCallGeneratorServices() {
        when(idamTokenGenerator.getPasswordTypeAuthorizationHeader()).thenReturn("idam token");
        when(serviceTokenGenerator.getServiceAuthToken()).thenReturn("service token");
        var headers = securityUtil.getAuthHeaders();
        assertThat(headers)
            .containsEntry("Authorization", "idam token")
            .containsEntry("ServiceAuthorization", "service token");
        verify(idamTokenGenerator, times(1)).getPasswordTypeAuthorizationHeader();
        verify(serviceTokenGenerator, times(1)).getServiceAuthToken();
    }
}
