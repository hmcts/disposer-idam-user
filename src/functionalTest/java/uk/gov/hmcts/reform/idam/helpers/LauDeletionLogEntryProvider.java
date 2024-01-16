package uk.gov.hmcts.reform.idam.helpers;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Component
@Slf4j
public class LauDeletionLogEntryProvider {

    @Inject
    private LauIdamBackendServiceProvider lauIdamBackendServiceProvider;

    public DeletionLog createDeletionLogLau(String userId, String email) {
        DeletedAccountsRequest request = generateDeletedAccountsRequest(userId, email);
        final DeletedUsersResponse deletedUsersResponse = lauIdamBackendServiceProvider.postLogEntry(request);
        return deletedUsersResponse.getDeletionLogs().get(0);
    }

    public DeletionLog createDeletionLogLau() {
        String userId = UUID.randomUUID().toString();
        String email = "DisposerRestorerTest-" + userId + "@example.org";
        DeletedAccountsRequest request = generateDeletedAccountsRequest(userId, email);
        final DeletedUsersResponse deletedUsersResponse = lauIdamBackendServiceProvider.postLogEntry(request);
        return deletedUsersResponse.getDeletionLogs().get(0);
    }

    public DeletedAccountsRequest generateDeletedAccountsRequest(String userId, String email) {
        DeletionLog deletedAccount = DeletionLog.builder()
            .userId(userId)
            .emailAddress(email)
            .firstName("LauRestorer-" + userId + "@example.org")
            .lastName("TestRestorer-")
            .deletionTimestamp(getTimeStamp(now().plusDays(2)))
            .build();


        DeletedAccountsRequest request = new DeletedAccountsRequest();
        request.setDeletionLogs(Arrays.asList(deletedAccount));
        return request;
    }

    private String getTimeStamp(LocalDateTime timeStamp) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return timeStamp.format(format);
    }

    public DeletedAccountsRequest generateDeletedAccountsRequestWithUserId(String userId, int days) {
        DeletionLog deletedAccount = DeletionLog.builder()
            .userId(userId)
            .emailAddress("DisposerRestorerTest-" + userId + "@example.org")
            .firstName("LauRestorer-" + userId + "@example.org")
            .lastName("TestRestorerExisting")
            .deletionTimestamp(getTimeStamp(now().plusDays(days)))
            .build();

        DeletedAccountsRequest request = new DeletedAccountsRequest();
        request.setDeletionLogs(Arrays.asList(deletedAccount));
        return request;
    }

    public List<DeletionLog> postDeletedLogEntryWithExistingUserId(String userId, int days) {
        DeletedAccountsRequest request = generateDeletedAccountsRequestWithUserId(userId, days);
        return lauIdamBackendServiceProvider.postLogEntry(request).getDeletionLogs();
    }

    public DeletedAccountsRequest generateDeletedAccountsRequestWithEmailAddress(String emailAddress, int days) {
        String id = UUID.randomUUID().toString();
        DeletionLog deletedAccount = DeletionLog.builder()
            .userId(id)
            .emailAddress(emailAddress)
            .firstName("LauRestorer-" + id + "@example.org")
            .lastName("TestRestorerExisting")
            .deletionTimestamp(getTimeStamp(now().plusDays(days)))
            .build();

        DeletedAccountsRequest request = new DeletedAccountsRequest();
        request.setDeletionLogs(Arrays.asList(deletedAccount));
        return request;
    }

    public List<DeletionLog> postDeletedLogEntryWithExistingEmail(String emailAddress, int days) {
        DeletedAccountsRequest request = generateDeletedAccountsRequestWithEmailAddress(emailAddress, days);
        return lauIdamBackendServiceProvider.postLogEntry(request).getDeletionLogs();
    }


}
