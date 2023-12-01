package uk.gov.hmcts.reform.idam.helpers;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;
import uk.gov.hmcts.reform.idam.util.SecurityUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static wiremock.org.eclipse.jetty.util.StringUtil.valueOf;

@RequiredArgsConstructor
@Component
@Slf4j
public class LauDeletionLogEntryProvider {

    private final SecurityUtil securityUtil;

    @Inject
    private LauIdamBackendServiceProvider lauIdamBackendServiceProvider;

    public List<DeletionLog> createDeletionLogLau() {
        securityUtil.generateTokens();
        DeletedAccountsRequest request = generateDeletedAccountsRequest();
        final DeletedUsersResponse deletedUsersResponse = lauIdamBackendServiceProvider.postLogEntry(request);
        return deletedUsersResponse.getDeletionLogs();
    }

    public DeletedAccountsRequest generateDeletedAccountsRequest() {
        String id = UUID.randomUUID().toString();
        DeletionLog deletedAccount = DeletionLog.builder()
            .userId(id)
            .emailAddress("DisposerRestorerTest-" + id + "@example.org")
            .firstName("LauRestorer-" + id + "@example.org")
            .lastName("TestRestorer-")
            .deletionTimestamp(getTimeStamp(valueOf(now().plusDays(2).toString())))
            .build();

        DeletedAccountsRequest request = new DeletedAccountsRequest();
        request.setDeletionLogs(Arrays.asList(deletedAccount));
        return request;
    }

    private String getTimeStamp(String timeStamp) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.sss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date date = format.parse(timeStamp);
            return dateFormat.format(date);
        } catch (ParseException e) {
            log.error("TimsStamp can't be parsed to : " + dateFormat);
            return "";
        }
    }

    public DeletedAccountsRequest generateDeletedAccountsRequestWithUserId(String userId, int days) {
        DeletionLog deletedAccount = DeletionLog.builder()
            .userId(userId)
            .emailAddress("DisposerRestorerTest-" + userId + "@example.org")
            .firstName("LauRestorer-" + userId + "@example.org")
            .lastName("TestRestorerExisting")
            .deletionTimestamp(getTimeStamp(valueOf(now().plusDays(days).toString())))
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
            .deletionTimestamp(getTimeStamp(valueOf(now().plusDays(days).toString())))
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
