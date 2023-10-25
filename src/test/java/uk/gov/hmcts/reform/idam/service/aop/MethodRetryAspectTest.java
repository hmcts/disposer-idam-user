package uk.gov.hmcts.reform.idam.service.aop;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MethodRetryAspectTest {

    @Mock
    ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    SecurityUtil securityUtil;

    @InjectMocks
    private MethodRetryAspect methodRetryAspect;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        methodRetryAspect = new MethodRetryAspect(securityUtil);
        MethodSignature signature = mock(MethodSignature.class);

        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(methodGetter());

    }

    @Test
    void testRetryProceeds() throws Throwable {
        methodRetryAspect.retry(proceedingJoinPoint);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    void testRetryCatches401() throws Throwable {
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        byte[] body = {};

        when(proceedingJoinPoint.proceed())
            .thenThrow(new FeignException.Unauthorized("Unauthorized", request, body, null))
            .thenReturn(new Object());

        methodRetryAspect.retry(proceedingJoinPoint);
        verify(securityUtil, times(1)).generateTokens();
        verify(proceedingJoinPoint, times(2)).proceed();
    }

    @Test
    void testRetryDoesNotRetryOnNon403() throws Throwable {
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        byte[] body = {};

        when(proceedingJoinPoint.proceed())
            .thenThrow(new FeignException.InternalServerError("Internal Server Error", request, body, null))
            .thenReturn(new Object());

        Exception thrown = assertThrows(FeignException.InternalServerError.class, () -> {
            methodRetryAspect.retry(proceedingJoinPoint);
        });

        verify(securityUtil, times(0)).generateTokens();
        verify(proceedingJoinPoint, times(1)).proceed();
        assertThat(thrown.getMessage()).contains("Internal Server Error");
    }

    public Method methodGetter() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("methodStub");
    }

    @Retry(retryAttempts = 2)
    public void methodStub() {
    }
}
