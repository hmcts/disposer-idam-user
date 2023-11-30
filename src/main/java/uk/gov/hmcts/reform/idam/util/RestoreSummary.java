package uk.gov.hmcts.reform.idam.util;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@Getter
public class RestoreSummary {

    private final List<String> successful = new LinkedList<>();
    private final List<String> failed = new LinkedList<>();
    private final List<String> failedToRestoreDueToReinstatedAndActiveAccount = new LinkedList<>();
    private final List<String> failedToRestoreDueToReinstatedAccount = new LinkedList<>();
    private final List<String> failedToRestoreDueToNewAccountWithSameEmail = new LinkedList<>();
    private final List<String> failedToRestoreDueToDuplicateEmail = new LinkedList<>();

    private long startTime;
    private long endTime;


    public void addSuccess(String userId) {
        successful.add(userId);
    }

    public void addFailed(String userId) {
        failed.add(userId);
    }

    public void addFailedToRestoreDueToReinstatedAndActiveAccount(String userId) {
        failedToRestoreDueToReinstatedAndActiveAccount.add(userId);
    }

    public void addFailedToRestoreDueToReinstatedAccount(String userId) {
        failedToRestoreDueToReinstatedAccount.add(userId);
    }

    public void addFailedToRestoreDueToNewAccountWithSameEmail(String userId) {
        failedToRestoreDueToNewAccountWithSameEmail.add(userId);
    }

    public void addFailedToRestoreDueToDuplicateEmail(String userId) {
        failedToRestoreDueToDuplicateEmail.add(userId);
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public void setEndTime() {
        endTime = System.currentTimeMillis();
    }
}
