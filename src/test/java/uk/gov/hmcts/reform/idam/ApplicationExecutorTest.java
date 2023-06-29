package uk.gov.hmcts.reform.idam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = ApplicationExecutor.class)
class ApplicationExecutorTest {
    @Mock
    ApplicationArguments applicationArguments;

    @Mock
    private IdamUserDisposerService idamUserDisposerService;

    @InjectMocks
    private ApplicationExecutor executor;

    @Test
    void shouldCallService() {
        ReflectionTestUtils.setField(executor, "isServiceEnabled", true);
        executor.run(applicationArguments);
        verify(idamUserDisposerService, times(1)).run();
    }
}
