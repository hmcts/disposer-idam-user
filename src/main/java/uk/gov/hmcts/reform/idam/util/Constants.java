package uk.gov.hmcts.reform.idam.util;

@SuppressWarnings("PMD.ConstantsInInterface")
public interface Constants {

    String STALE_USERS_PATH = "/api/v1/staleUsers";

    String STALE_USERS_DELETE_PATH = "/api/v2/users/";

    String ROLE_ASSIGNMENTS_PATH = "/am/role-assignments/query";

    @SuppressWarnings("checkstyle:linelength")
    String ROLE_ASSIGNMENTS_CONTENT_TYPE = "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=2.0";

}
