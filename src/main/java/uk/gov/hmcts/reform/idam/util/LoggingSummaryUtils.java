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
        valueMappings.put("isSimulation", parameterResolver.isSimulation());
        valueMappings.put("batchSize", parameterResolver.getBatchSize());
        valueMappings.put("requestLimit", parameterResolver.getRequestLimit());

        log.info(format(template, valueMappings));
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
