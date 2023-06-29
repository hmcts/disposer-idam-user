package uk.gov.hmcts.reform.idam.parameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.TooManyMethods")
class DefaultParameterResolverTest {

    private static final String IDAM_API_URL = "idamHost";
    private static final String IDAM_API_USERNAME = "idamUsername";
    private static final String IDAM_API_USER_PASSWORD = "idamPassword";

    private static final String ROLE_ASSIGNMENT_CONTENT_TYPE = "roleAssignmentsContentType";

    private static final String BATCH_SIZE = "batchSize";

    private static final String REQUESTS_LIMIT = "requestsLimit";

    private static final String READ_TIMEOUT = "readTimeout";

    private static final String CONNECT_TIMEOUT = "connectTimeout";

    private static final String STALE_USERS_PATH = "staleUsersPath";

    private static final String ROLE_ASSIGNMENTS_PATH = "roleAssignmentsPath";

    private final DefaultParameterResolver resolver = new DefaultParameterResolver();

    @BeforeEach
    public void initMock() {
        ReflectionTestUtils.setField(resolver, IDAM_API_URL, "http://locahost:5000");
        ReflectionTestUtils.setField(resolver, IDAM_API_USERNAME, "user@example.org");
        ReflectionTestUtils.setField(resolver, IDAM_API_USER_PASSWORD, "password");
        ReflectionTestUtils.setField(resolver, ROLE_ASSIGNMENT_CONTENT_TYPE, "application/json");
        ReflectionTestUtils.setField(resolver, BATCH_SIZE, 100);
        ReflectionTestUtils.setField(resolver, REQUESTS_LIMIT, 10);
        ReflectionTestUtils.setField(resolver, READ_TIMEOUT, 60_000);
        ReflectionTestUtils.setField(resolver, CONNECT_TIMEOUT, 60_000);
        ReflectionTestUtils.setField(resolver, STALE_USERS_PATH, "/staleUsers");
        ReflectionTestUtils.setField(resolver, ROLE_ASSIGNMENTS_PATH, "/roleAssignments/path");
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

    @Test
    void shouldGetRoleAssignmentsContentType() {
        assertThat(resolver.getRoleAssignmentsContentType()).isEqualTo("application/json");
    }

    @Test
    void shouldGetBatchSize() {
        assertThat(resolver.getBatchSize()).isEqualTo(100);
    }

    @Test
    void shouldGetRequestsLimit() {
        assertThat(resolver.getRequestsLimit()).isEqualTo(10);
    }

    @Test
    void shouldGetReadTimeout() {
        assertThat(resolver.getReadTimeout()).isEqualTo(60_000);
    }

    @Test
    void shouldGetConnectTimeout() {
        assertThat(resolver.getConnectTimeout()).isEqualTo(60_000);
    }

    @Test
    void shouldGetStaleUsersPath() {
        assertThat(resolver.getStaleUsersPath()).isEqualTo("/staleUsers");
    }

    @Test
    void shouldGetRoleAssignmentsPath() {
        assertThat(resolver.getRoleAssignmentsPath()).isEqualTo("/roleAssignments/path");
    }
}
