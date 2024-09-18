package uk.gov.hmcts.reform.idam.parameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.TooManyMethods")
class ParameterResolverTest {

    private static final String IDAM_API_URL = "idamHost";
    private static final String BATCH_SIZE = "batchSize";
    private static final String REQUEST_LIMIT = "requestLimit";

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CLIENT_USER_NAME = "clientUserName";
    private static final String CLIENT_PASSWORD = "clientPassword";
    private static final String REDIRECT_URI = "redirectUri";
    private static final String IS_SIMULATION_MODE = "isSimulation";

    private final ParameterResolver resolver = new ParameterResolver();

    @BeforeEach
    public void initMock() {
        ReflectionTestUtils.setField(resolver, IDAM_API_URL, "http://locahost:5000");
        ReflectionTestUtils.setField(resolver, BATCH_SIZE, 100);
        ReflectionTestUtils.setField(resolver, REQUEST_LIMIT, 10);
        ReflectionTestUtils.setField(resolver, CLIENT_ID, "client id");
        ReflectionTestUtils.setField(resolver, CLIENT_SECRET, "client secret");
        ReflectionTestUtils.setField(resolver, "citizenRole", "disposer-test");
        ReflectionTestUtils.setField(resolver, "additionalIdamCitizenRoles", Optional.of(Set.of("role1", "role2")));
        ReflectionTestUtils.setField(resolver, "citizenRolesPattern", "pattern");
        ReflectionTestUtils.setField(resolver, CLIENT_USER_NAME, "user@example.org");
        ReflectionTestUtils.setField(resolver, CLIENT_PASSWORD, "client password");
        ReflectionTestUtils.setField(resolver, REDIRECT_URI, "redirect.uri");
        ReflectionTestUtils.setField(resolver, IS_SIMULATION_MODE, true);

    }

    @Test
    void shouldGetIdamHost() {
        assertThat(resolver.getIdamHost()).isEqualTo("http://locahost:5000");
    }

    @Test
    void shouldGetBatchSize() {
        assertThat(resolver.getBatchSize()).isEqualTo(100);
    }

    @Test
    void shouldGetRequesLimit() {
        assertThat(resolver.getRequestLimit()).isEqualTo(10);
    }

    @Test
    void shouldGetClientId() {
        assertThat(resolver.getClientId()).isEqualTo("client id");
    }

    @Test
    void shouldGetClientSecret() {
        assertThat(resolver.getClientSecret()).isEqualTo("client secret");
    }

    @Test
    void shouldGetCitizenRoleToDelete() {
        assertThat(resolver.getCitizenRole()).isEqualTo("disposer-test");
    }

    @Test
    void shouldGetAdditionalIdamCitizenRoles() {
        assertThat(resolver.getAdditionalIdamCitizenRoles()).isEqualTo(Optional.of(Set.of("role1", "role2")));
    }

    @Test
    void shouldGetCitizenRolesPattern() {
        assertThat(resolver.getCitizenRolesPattern()).isEqualTo("pattern");
    }

    @Test
    void shouldGetClientUserName() {
        assertThat(resolver.getClientUserName()).isEqualTo("user@example.org");
    }

    @Test
    void shouldGetClientPassword() {
        assertThat(resolver.getClientPassword()).isEqualTo("client password");
    }

    @Test
    void shouldGetRedirectUri() {
        assertThat(resolver.getRedirectUri()).isEqualTo("redirect.uri");
    }

    @Test
    void shouldGetIsSimulationMode() {
        assertThat(resolver.getIsSimulation()).isTrue();
    }
}
