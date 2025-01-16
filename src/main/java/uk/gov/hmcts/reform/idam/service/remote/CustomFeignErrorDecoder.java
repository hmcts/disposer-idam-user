package uk.gov.hmcts.reform.idam.service.remote;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        FeignException exception = FeignException.errorStatus(methodKey, response);
        log.info("Feign response status: {}, message - {}", status, exception.getMessage());
        if (status >= 400) {
            return new RetryableException(
                response.status(),
                exception.getMessage(),
                response.request().httpMethod(),
                (Long) null,  // unix timestamp, if given retries after that point in time
                response.request()
            );
        }
        return exception;
    }
}
