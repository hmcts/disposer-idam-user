package uk.gov.hmcts.reform.idam.util;

@SuppressWarnings("java:S1075")
public final class Constants {

    public static final String STALE_USERS_PATH = "/api/v1/staleUsers";

    public static final String ROLE_ASSIGNMENTS_PATH = "/am/role-assignments";
    public static final String ROLE_ASSIGNMENTS_QUERY_PATH = ROLE_ASSIGNMENTS_PATH + "/query";

    @SuppressWarnings("checkstyle:linelength")
    public static final String ROLE_ASSIGNMENTS_CONTENT_TYPE = "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=2.0";

    private Constants() {
    }
}
