package uk.gov.hmcts.reform.idam.parameter;

public interface ParameterResolver {

    String getIdamHost();

    int getBatchSize();

    int getRequestsLimit();

}
