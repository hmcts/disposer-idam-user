package uk.gov.hmcts.reform.idam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.service.IdamDuplicateUserMergerService;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;
import uk.gov.hmcts.reform.idam.service.IdamUserRestorerService;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = ApplicationExecutor.class)
class ApplicationExecutorTest {
    @Mock
    ApplicationArguments applicationArguments;

    @Mock
    private IdamUserDisposerService idamUserDisposerService;

    @Mock
    private IdamUserRestorerService idamUserRestorerService;

    @Mock
    private IdamDuplicateUserMergerService idamDuplicateUserMergerService;

    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    private ApplicationExecutor executor;

    @Test
    void shouldCallDisposerService() {
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", true);
        ReflectionTestUtils.setField(executor, "isRestorerEnabled", false);
        executor.run(applicationArguments);
        verify(idamUserDisposerService, times(1)).run();
        verify(idamUserRestorerService, times(0)).run();
        verify(idamDuplicateUserMergerService, times(0)).run();
        verify(securityUtil, times(1)).generateTokens();
    }

    @Test
    void shouldCallRestorerService() {
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", false);
        ReflectionTestUtils.setField(executor, "isRestorerEnabled", true);
        executor.run(applicationArguments);
        verify(idamUserRestorerService, times(1)).run();
        verify(idamUserDisposerService, times(0)).run();
        verify(idamDuplicateUserMergerService, times(0)).run();
        verify(securityUtil, times(1)).generateTokens();
    }

    @Test
    void shouldNotRunAnyIfBothDisabled() {
        ReflectionTestUtils.setField(executor, "isRestorerEnabled", false);
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", false);
        ReflectionTestUtils.setField(executor, "isDuplicateUserEnabled", false);
        executor.run(applicationArguments);
        verify(idamUserDisposerService, times(0)).run();
        verify(idamUserRestorerService, times(0)).run();
        verify(idamDuplicateUserMergerService, times(0)).run();
        verify(securityUtil, times(0)).generateTokens();
    }

    @Test
    void shouldCallDuplicateUserService() {
        ReflectionTestUtils.setField(executor, "isDisposerEnabled", false);
        ReflectionTestUtils.setField(executor, "isRestorerEnabled", false);
        ReflectionTestUtils.setField(executor, "isDuplicateUserEnabled", true);
        executor.run(applicationArguments);
        verify(idamUserRestorerService, times(0)).run();
        verify(idamUserDisposerService, times(0)).run();
        verify(idamDuplicateUserMergerService, times(1)).run();
        verify(securityUtil, times(1)).generateTokens();
    }
}
