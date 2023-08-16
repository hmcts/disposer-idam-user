package uk.gov.hmcts.reform.idam.service.remote;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

@Component
@RequiredArgsConstructor
public class AuthRequestInterceptor implements RequestInterceptor {

    private final SecurityUtil securityUtil;

    @Override
    public void apply(RequestTemplate template) {
        //template.header("Authorization", securityUtil.getIdamClientToken());
        template.header("ServiceAuthorization", securityUtil.getServiceAuthorization());
    }
}
