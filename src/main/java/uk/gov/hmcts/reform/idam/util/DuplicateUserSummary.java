package uk.gov.hmcts.reform.idam.util;


import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class DuplicateUserSummary {

    private long startTime;
    private long endTime;

    // Users that were not found in idam, should not have these
    private int noUserInIdam;

    // Users that returned multiple accounts with the same email (in theory should not have these)
    private int multipleUsersInIdamMatchEmail;

    // Users that idam id match our id on the same email - should be the majority
    private int emailAndIdsMatch;

    // Users that have different id in idam on the same email - expected around 5-6k
    private int multipleIdsOnEmail;

    // Users that failed to merge
    private int failedMerge;

    // Archived users that have no role assignments
    private int noRoleAssigmentsOnArchived;

    // Successful role assignment merges
    private int merged;

    private boolean dryRunMode;

    public void setIsDryRunMode(boolean dryRunMode) {
        this.dryRunMode = dryRunMode;
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public void setEndTime() {
        endTime = System.currentTimeMillis();
    }

    public void increaseMatchedIds() {
        this.emailAndIdsMatch++;
    }

    public void increaseEmailMultipleIds() {
        this.multipleIdsOnEmail++;
    }

    public void increaseNoMatches() {
        this.noUserInIdam++;
    }

    public void increaseMultipleMatches() {
        this.multipleUsersInIdamMatchEmail++;
    }

    public void increaseFailedMerge() {
        this.failedMerge++;
    }

    public void increaseNoRoleAssignmentsOnUser() {
        this.noRoleAssigmentsOnArchived++;
    }

    public void increaseMerged() {
        this.merged++;
    }
}
