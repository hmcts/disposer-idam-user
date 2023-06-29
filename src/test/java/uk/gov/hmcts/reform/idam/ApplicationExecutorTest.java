package uk.gov.hmcts.reform.idam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.StaleUsersService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = ApplicationExecutor.class)
class ApplicationExecutorTest {
    @Mock
    ApplicationArguments applicationArguments;

    @Mock
    private StaleUsersService staleUsersService;

    @InjectMocks
    private ApplicationExecutor executor;

    @Test
    void shouldCallService() throws Exception {
        ReflectionTestUtils.setField(executor, "serviceEnabled", true);
        executor.run(applicationArguments);
        verify(staleUsersService, times(1)).retrieveStaleUsers();
    }
}
