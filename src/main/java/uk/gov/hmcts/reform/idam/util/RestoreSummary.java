package uk.gov.hmcts.reform.idam.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
public class RestoreSummary {

    private int totalProcessed;
    private final List<String> successful = new LinkedList<>();
    private final List<String> failed = new LinkedList<>();
    private final List<String> failedToRestoreDueToReinstatedAndActiveAccount = new LinkedList<>();
    private final List<String> failedToRestoreDueToReinstatedAccount = new LinkedList<>();
    private final List<String> failedToRestoreDueToNewAccountWithSameEmail = new LinkedList<>();
    private final List<String> failedToRestoreDueToDuplicateEmail = new LinkedList<>();
    private int requestsMade;
    private Map<String, String> idamDeletionResponse = new ConcurrentHashMap<>();

    @Value("${restorer.start.page}")
    private int startPage;

    @Value("${restorer.batch.size}")
    private int batchSize;

    private long startTime;
    private long endTime;


    public void addProcessedNumber(int batch) {
        totalProcessed += batch;
    }

    public void addSuccess(String userId) {
        successful.add(userId);
    }

    public void addFailed(String userId) {
        failed.add(userId);
    }

    public void addRestoredUserResponse(String userId, String response) {
        idamDeletionResponse.put(userId, response);
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

    public void increaseRequestsMade() {
        this.requestsMade++;
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

    public int getTotalFailed() {
        return failed.size()
            + failedToRestoreDueToReinstatedAndActiveAccount.size()
            + failedToRestoreDueToReinstatedAccount.size()
            + failedToRestoreDueToNewAccountWithSameEmail.size()
            + failedToRestoreDueToDuplicateEmail.size();
    }
}
