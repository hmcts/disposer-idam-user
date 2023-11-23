package uk.gov.hmcts.reform.idam.util;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class RestoreSummary {

    private final List<String> successful;
    private final List<String> failedToRestore;

    public RestoreSummary() {
        successful = new LinkedList<>();
        failedToRestore = new LinkedList<>();
    }

    public void addSuccess(String userId) {
        successful.add(userId);
    }

    public void addFailedRestore(String userId) {
        failedToRestore.add(userId);
    }
}
