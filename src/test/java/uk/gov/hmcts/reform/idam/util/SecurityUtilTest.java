package uk.gov.hmcts.reform.idam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        securityUtil.generateTokens();
        verify(idamTokenGenerator, times(1)).generateIdamToken();
        verify(serviceTokenGenerator, times(1)).generateServiceToken();
    }
}
