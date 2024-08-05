package uk.gov.hmcts.reform.idam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = ApplicationExecutor.class)
@ExtendWith(OutputCaptureExtension.class)
class ApplicationExecutorTest {
    @Mock
    ApplicationArguments applicationArguments;

    @Mock
    private IdamUserDisposerService idamUserDisposerService;

    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    private ApplicationExecutor executor;

    @Test
    void shouldCallDisposerService() {
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", true);
        executor.run(applicationArguments);
        verify(idamUserDisposerService, times(1)).run();
        verify(securityUtil, times(1)).generateTokens();
    }

    @Test
    void shouldNotRunAnyIfBothDisabled() {
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", false);
        executor.run(applicationArguments);
        verify(idamUserDisposerService, times(0)).run();
        verify(securityUtil, times(0)).generateTokens();
    }

    @Test()
    void shouldCatchExceptionAndLogError(CapturedOutput output) {

        // given
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", true);
        Mockito.doThrow(new IllegalArgumentException("Exception to test alert"))
            .when(idamUserDisposerService).run();

        // when
        executor.run(applicationArguments);

        // then
        verify(idamUserDisposerService, times(1)).run();
        assertThat(output).contains("Error executing Disposer Idam User service");

    }
}
