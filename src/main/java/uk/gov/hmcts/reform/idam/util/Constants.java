package uk.gov.hmcts.reform.idam.util;

@SuppressWarnings("PMD.ConstantsInInterface")
public interface Constants {

    String STALE_USERS_PATH = "/api/v2/staleUsers";

    String ROLE_ASSIGNMENTS_PATH = "/am/role-assignments/query";

    String DELETE_USER_PATH = "/api/v1/staleUsers";

    @SuppressWarnings("checkstyle:linelength")
    String ROLE_ASSIGNMENTS_CONTENT_TYPE = "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=2.0";

}
