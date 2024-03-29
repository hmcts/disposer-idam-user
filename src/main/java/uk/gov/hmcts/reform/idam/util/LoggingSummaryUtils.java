package uk.gov.hmcts.reform.idam.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoggingSummaryUtils {
    private final ParameterResolver parameterResolver;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void logSummary(long startTime, long endTime, int processedUsers, int deletedUsers, int failedDeletions) {
        final String template = """
            Disposer Idam User Summary :
            -----------------------------------------------------------
            Disposer Start Time :       | ${startTime}
            Disposer End Time :         | ${endTime}
            Disposer Execution Time :   | ${totalTime}
            Total Processed Users :     | ${processedUsers}
            Total Deleted Users :       | ${deletedUsers}
            Failed deletions :          | ${failedDeletions}
            Total Undeleted Users :     | ${undeletedUsers}
            Is Simulation Mode :        | ${isSimulation}
            Batch Size :                | ${batchSize}
            Request Limit :             | ${requestLimit}
            -----------------------------------------------------------
            """;
        final Map<String, Object> valueMappings = createRuntimeStats(startTime, endTime);
        valueMappings.put("processedUsers", processedUsers);
        valueMappings.put("deletedUsers", deletedUsers - failedDeletions);
        valueMappings.put("failedDeletions", failedDeletions);
        valueMappings.put("undeletedUsers", processedUsers - deletedUsers);
        valueMappings.put("isSimulation", parameterResolver.getIsSimulation());
        valueMappings.put("batchSize", parameterResolver.getBatchSize());
        valueMappings.put("requestLimit", parameterResolver.getRequestLimit());

        log.info(format(template, valueMappings));
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    public String createRestorerStatistics(RestoreSummary summary) {
        final String template = """

            Restorer Idam User Summary:
            -------------------------------------------------------------------
            Restorer start time:                            | ${startTime}
            Restorer end time:                              | ${endTime}
            Total run time:                                 | ${totalTime}

            -------------------------------------------------------------------
            Processed deletion logs:                        | ${processed}
            Total restored users:                           | ${restored}
            Total failed users:                             | ${totalFailed}

            -------------------------------------------------------------------
            Requests made:                                  | ${requestsMade}
            Start page:                                     | ${startPage}
            Page size:                                      | ${batchSize}
            -------------------------------------------------------------------
            Total 409 due to user id conflict:              | ${conflictUserId}
            Total 409 due to archived user id conflict:     | ${conflictArchivedUserId}
            Total 409 due to email conflict:                | ${conflictEmail}
            Total 409 due to archived email conflict:       | ${conflictArchivedEmail}
            Total failed due to other reasons:              | ${failedOther}
            """;

        final Map<String, Object> valueMappings = createRuntimeStats(summary.getStartTime(), summary.getEndTime());

        valueMappings.put("processed", summary.getTotalProcessed());
        valueMappings.put("restored", summary.getSuccessful().size());
        valueMappings.put("conflictUserId", summary.getFailedToRestoreDueToReinstatedAndActiveAccount().size());
        valueMappings.put("conflictArchivedUserId", summary.getFailedToRestoreDueToReinstatedAccount().size());
        valueMappings.put("conflictEmail", summary.getFailedToRestoreDueToNewAccountWithSameEmail().size());
        valueMappings.put("conflictArchivedEmail", summary.getFailedToRestoreDueToDuplicateEmail().size());
        valueMappings.put("failedOther", summary.getFailed().size());
        valueMappings.put("totalFailed", summary.getTotalFailed());
        valueMappings.put("requestsMade", summary.getRequestsMade());
        valueMappings.put("startPage", summary.getStartPage());
        valueMappings.put("batchSize", summary.getBatchSize());

        return format(template, valueMappings);
    }

    public String createMergerStatistics(DuplicateUserSummary summary) {
        final String template = """

            User role assignments merger Summary:
            -----------------------------------------------------------------------
            Merger start time:                                  | ${startTime}
            Merger end time:                                    | ${endTime}
            Total run time:                                     | ${totalTime}
            ----------------------------------------------------|------------------
            Users not found in IDAM:                            | ${noUserInIdam}
            Users that have multiple accounts in IDAM:          | ${multipleUsersInIdamMatchEmail}
            Users that have a single account on email:          | ${emailAndIdsMatch}
            Users that have created new account since deletion: | ${multipleIdsOnEmail}
            Users that have failed role assignments merge:      | ${failedMerge}
            Users that have no roles on archived account:       | ${noRoleAssigmentsOnArchived}
                                                                |
            Dry run?                                            | ${isDryRun}
            Merged                                              | ${merged}
            """;

        final Map<String, Object> valueMappings = createRuntimeStats(summary.getStartTime(), summary.getEndTime());

        valueMappings.put("noUserInIdam", summary.getNoUserInIdam());
        valueMappings.put("multipleUsersInIdamMatchEmail", summary.getMultipleUsersInIdamMatchEmail());
        valueMappings.put("emailAndIdsMatch", summary.getEmailAndIdsMatch());
        valueMappings.put("multipleIdsOnEmail", summary.getMultipleIdsOnEmail());
        valueMappings.put("failedMerge", summary.getFailedMerge());
        valueMappings.put("noRoleAssigmentsOnArchived", summary.getNoRoleAssigmentsOnArchived());
        valueMappings.put("merged", summary.getMerged());
        valueMappings.put("isDryRun", summary.isDryRunMode());

        return format(template, valueMappings);
    }

    private Map<String, Object> createRuntimeStats(long startTime, long endTime) {
        final Map<String, Object> valueMappings = new ConcurrentHashMap<>();
        final long executionTime  =  endTime - startTime;
        valueMappings.put("startTime", dateFormat.format(new Date(startTime)));
        valueMappings.put("endTime", dateFormat.format(new Date(endTime)));
        valueMappings.put("totalTime", getDurationFromLong(executionTime));
        return valueMappings;
    }

    private static String format(String template, Map<String, Object> parameters) {
        StringBuilder newTemplate = new StringBuilder(template);
        List<Object> valueList = new ArrayList<>();

        Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);

            String paramName = "${" + key + "}";
            int index = newTemplate.indexOf(paramName);
            if (index != -1) {
                newTemplate.replace(index, index + paramName.length(), "%s");
                valueList.add(parameters.get(key));
            }
        }

        return String.format(newTemplate.toString(), valueList.toArray());
    }

    @SuppressWarnings("PMD.LawOfDemeter")
    public String getDurationFromLong(long duration) {
        long hh = TimeUnit.MILLISECONDS.toHours(duration);
        long mm = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long ss = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

}
