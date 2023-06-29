package uk.gov.hmcts.reform.idam.parameter;

public interface ParameterResolver {

    String getIdamHost();

    String getIdamUsername();

    String getIdamPassword();

    String getRoleAssignmentsContentType();

    int getBatchSize();

    int getRequestsLimit();

    int getReadTimeout();

    int getConnectTimeout();

    String getStaleUsersPath();

    String getRoleAssignmentsPath();
}
