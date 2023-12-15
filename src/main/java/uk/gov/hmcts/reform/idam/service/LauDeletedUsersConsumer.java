package uk.gov.hmcts.reform.idam.service;

import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;

import java.util.List;

public interface LauDeletedUsersConsumer {
    void consumeLauDeletedUsers(List<DeletionLog> deletedUsers);
}
