package uk.gov.hmcts.reform.idam.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idam")
@Getter
@Setter
public class IdamProperties {

    private ApiProperties api = new ApiProperties();
    private S2sAuth s2sAuth = new S2sAuth();
    private Client client =  new Client();

    @Getter
    @Setter
    public static class ApiProperties {
        int port;
        String url;
    }

    @Getter
    @Setter
    public static class S2sAuth {
        String name;
        String url;
        String secret;
    }

    @Getter
    @Setter
    public static class Client {
        String id;
        String secret;
        String username;
        String password;
        String redirectUri;
    }

}
