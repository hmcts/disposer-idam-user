package uk.gov.hmcts.reform.idam.service.remote;

import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
public class RetryableFeignConfig {

    @Value("${service.feign-retry-min-wait:60}")
    private int periodToWait;

    @Bean
    public Retryer retryer() {
        long period = SECONDS.toMillis(periodToWait);
        long maxPeriod = SECONDS.toMillis(300);
        // default backoff calculation formula:
        // Math.min(period * Math.pow(1.5, attempt - 1), maxPeriod)
        return new Retryer.Default(period, maxPeriod, 3);
    }
}
