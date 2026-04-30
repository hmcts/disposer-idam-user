package uk.gov.hmcts.reform.idam.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.PropertyPlaceholderHelper;
import uk.gov.hmcts.reform.idam.config.CcdProperties;
import uk.gov.hmcts.reform.idam.config.StaleUsersProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoggingSummaryUtils {
    private final StaleUsersProperties staleUsersProperties;
    private final CcdProperties ccdProperties;

    private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

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
            Stale Users Batch Size :    | ${staleUsersBatchSize}
            RAS Batch Size :            | ${rasBatchSize}
            Request Limit :             | ${requestLimit}
            -----------------------------------------------------------
            """;
        final Map<String, Object> valueMappings = createRuntimeStats(startTime, endTime);
        valueMappings.put("processedUsers", processedUsers);
        valueMappings.put("deletedUsers", deletedUsers - failedDeletions);
        valueMappings.put("failedDeletions", failedDeletions);
        valueMappings.put("undeletedUsers", processedUsers - deletedUsers);
        valueMappings.put("isSimulation", staleUsersProperties.isSimulation());
        valueMappings.put("staleUsersBatchSize", staleUsersProperties.getBatchSize());
        valueMappings.put("rasBatchSize", ccdProperties.getRoleAssignment().getBatchSize());
        valueMappings.put("requestLimit", staleUsersProperties.getRequests().getLimit());

        log.info(PLACEHOLDER_HELPER.replacePlaceholders(template, name -> String.valueOf(valueMappings.get(name))));
    }

    private Map<String, Object> createRuntimeStats(long startTime, long endTime) {
        final Map<String, Object> valueMappings = new ConcurrentHashMap<>();
        final long executionTime  =  endTime - startTime;
        valueMappings.put("startTime", dateFormat.format(new Date(startTime)));
        valueMappings.put("endTime", dateFormat.format(new Date(endTime)));
        valueMappings.put("totalTime", getDurationFromLong(executionTime));
        return valueMappings;
    }

    public String getDurationFromLong(long duration) {
        long hh = TimeUnit.MILLISECONDS.toHours(duration);
        long mm = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long ss = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

}
