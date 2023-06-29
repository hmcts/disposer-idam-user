package uk.gov.hmcts.reform.idam.parameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultParameterResolverTest {

    private static final String IDAM_API_URL = "idamHost";
    private static final String IDAM_API_USERNAME = "idamApiUsername";
    private static final String IDAM_API_USER_PASSWORD = "idamApiPassword";

    private final DefaultParameterResolver resolver = new DefaultParameterResolver();

    @BeforeEach
    public void initMock() {
        ReflectionTestUtils.setField(resolver, IDAM_API_URL, "http://locahost:5000");
        ReflectionTestUtils.setField(resolver, IDAM_API_USERNAME, "user@example.org");
        ReflectionTestUtils.setField(resolver, IDAM_API_USER_PASSWORD, "password");
    }

    @Test
    void shouldGetIdamHost() {
        assertThat(resolver.getIdamHost()).isEqualTo("http://locahost:5000");
    }

    @Test
    void shouldGetIdamUsername() {
        assertThat(resolver.getIdamUsername()).isEqualTo("user@example.org");
    }

    @Test
    void shouldGetIdamUserPassword() {
        assertThat(resolver.getIdamPassword()).isEqualTo("password");
    }
}
