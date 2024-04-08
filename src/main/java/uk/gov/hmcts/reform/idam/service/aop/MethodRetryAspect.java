package uk.gov.hmcts.reform.idam.service.aop;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.lang.reflect.Method;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class MethodRetryAspect {

    private final SecurityUtil securityUtil;

    @Around("@annotation(uk.gov.hmcts.reform.idam.service.aop.Retry)")
    @SuppressWarnings("PMD.LawOfDemeter")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        Retry retry = method.getDeclaredAnnotation(Retry.class);
        int retryAttempts = retry.retryAttempts();
        Object result = null;
        boolean successful = false;
        do {
            retryAttempts--;
            try {
                result = joinPoint.proceed();
                successful = true;
            } catch (FeignException fe) {
                if (retryAttempts >= 0 && fe.status() == HttpStatus.SC_UNAUTHORIZED) {
                    log.info("Method {} with {} arguments threw FeignException ",
                        joinPoint.getSignature(),
                        joinPoint.getArgs());
                    securityUtil.generateTokens();
                } else {
                    log.error("IdamClient threw exception", fe);
                    throw fe;
                }
            }
        } while (!successful);

        return result;
    }
}
