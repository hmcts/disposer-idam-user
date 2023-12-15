package uk.gov.hmcts.reform.idam.util;

@SuppressWarnings({"PMD.ConstantsInInterface", "java:S1075"})
public interface Constants {

    String STALE_USERS_PATH = "/api/v1/staleUsers";

    String IDAM_QUERY_PATH = "/api/v1/users";

    String ROLE_ASSIGNMENTS_PATH = "/am/role-assignments/query";

    @SuppressWarnings("checkstyle:linelength")
    String ROLE_ASSIGNMENTS_CONTENT_TYPE = "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json;charset=UTF-8;version=2.0";

    String LAU_GET_DELETED_USERS_PATH = "/audit/getAllDeletedAccounts";

    String QUERY_PARAM = "query";
    String QUERY_SIZE = "size";

}
