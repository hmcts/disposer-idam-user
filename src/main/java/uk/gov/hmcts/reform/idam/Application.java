package uk.gov.hmcts.reform.idam;

import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.idam.service.remote.CustomFeignErrorDecoder;

import java.time.Clock;

@Slf4j
@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
public class Application {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ErrorDecoder customFeignErrorDecoder() {
        return new CustomFeignErrorDecoder();
    }

    public static void main(final String[] args) {
        final ApplicationContext context = SpringApplication.run(Application.class);
        SpringApplication.exit(context);
    }
}
