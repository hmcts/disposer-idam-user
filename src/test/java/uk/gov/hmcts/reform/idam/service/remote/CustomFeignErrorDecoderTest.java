package uk.gov.hmcts.reform.idam.service.remote;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomFeignErrorDecoderTest {

    private final ErrorDecoder errorDecoder = new CustomFeignErrorDecoder();

    @ParameterizedTest
    @CsvSource({
        "400, Bad Request",
        "401, Unauthorized",
        "403, Forbidden",
        "500, Internal Server Error",
        "502, Bad Gateway",
        "503, Service Unavailable",
        "504, Gateway Timeout"
    })
    void testDecodeErrorReturnsRetryable(int status, String reason) {

        Response response = Response.builder()
            .status(status)
            .reason(reason)
            .request(Request.create(Request.HttpMethod.GET, "/", Map.of(), null, new RequestTemplate()))
            .build();
        Exception exc = errorDecoder.decode("methodKey", response);
        assertThat(exc).isInstanceOf(RetryableException.class);
    }

    @Test
    void testDecodeNonRetryableException() {
        Response response = Response.builder()
            .status(302)
            .reason("Found")
            .request(Request.create(Request.HttpMethod.GET, "/", Map.of(), null, new RequestTemplate()))
            .build();
        Exception exc = errorDecoder.decode("methodKey", response);
        assertThat(exc)
            .isInstanceOf(feign.FeignException.class)
            .isNotInstanceOf(RetryableException.class);
    }
}
