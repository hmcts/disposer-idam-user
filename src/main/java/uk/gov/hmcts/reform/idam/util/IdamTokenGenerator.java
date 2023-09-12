package uk.gov.hmcts.reform.idam.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.exception.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;
import uk.gov.hmcts.reform.idam.service.remote.client.IdamClient;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class IdamTokenGenerator {

    public static final String BEARER = "Bearer ";
    public static final String ROLE_ASSIGNMENTS_GRANT_TYPE = "password";
    public static final String IDAM_GRANT_TYPE = "client_credentials";
    public static final String IDAM_SCOPE = "archive-user view-archived-user delete-archived-user";
    public static final String ROLE_ASSIGNMENT_SCOPE = "profile roles";


    private final IdamClient idamClient;
    private final ParameterResolver parameterResolver;

    private String idamClientToken = "token";
    private String roleAssignmentsClientToken = "token";

    public void generateIdamToken() {
        try {
            TokenResponse tokenResponse = idamClient.getToken(
                    parameterResolver.getClientId(),
                    parameterResolver.getClientSecret(),
                    null,
                    IDAM_GRANT_TYPE,
                    parameterResolver.getClientUserName(),
                    parameterResolver.getClientPassword(),
                    IDAM_SCOPE
            );
            idamClientToken = tokenResponse.accessToken;

        } catch (final Exception exception) {
            String msg = String.format("Unable to generate IDAM token due to error - %s", exception.getMessage());
            log.error(msg, exception);
            throw new IdamAuthTokenGenerationException(msg, exception);
        }
    }

    public void generateRoleAssignmentIdamToken() {
        try {
            TokenResponse tokenResponse = idamClient.getToken(
                    parameterResolver.getClientId(),
                    parameterResolver.getClientSecret(),
                    parameterResolver.getRedirectUri(),
                    ROLE_ASSIGNMENTS_GRANT_TYPE,
                    parameterResolver.getClientUserName(),
                    parameterResolver.getClientPassword(),
                    ROLE_ASSIGNMENT_SCOPE
            );
            roleAssignmentsClientToken = tokenResponse.accessToken;

        } catch (final Exception exception) {
            String msg = String.format("Unable to generate Role Assignment IDAM token due to error - %s",
                    exception.getMessage());
            log.error(msg, exception);
            throw new IdamAuthTokenGenerationException(msg, exception);
        }
    }

    public String getIdamAuthorizationHeader() {
        return BEARER + idamClientToken;
    }

    public String getRoleAssignmentAuthorizationHeader() {
        return BEARER + roleAssignmentsClientToken;
    }
}
