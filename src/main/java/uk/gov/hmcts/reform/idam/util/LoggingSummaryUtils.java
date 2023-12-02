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

    private static final String DOTTED_LINE = "-----------------------------------------------------------";
    private static final String CR_STRING = "\r\n";
    private static final String TAB_STRING = "| ";
    private static final String FORMAT_STR_LENGTH_30 = "%1$-30s";
    private static final String FORMAT_STR_LENGTH_10 = "%1$-10s";
    private static final String SUMMARY_HEADING_STRING = "\r\nDisposer Idam User Summary : ";
    private static final String DISPOSER_START_TIME = "\r\nDisposer Start Time : ";
    private static final String DISPOSER_END_TIME = "\r\nDisposer End Time : ";
    private static final String DISPOSER_EXECUTION_TIME = "\r\nDisposer Execution Time : ";
    private static final String TOTAL_PROCESSED_USERS = "\r\nTotal Processed Users : ";
    private static final String TOTAL_DELETED_USERS = "\r\nTotal Deleted Users : ";
    private static final String TOTAL_ERRORED_DELETIONS = "\r\nFailed deletions :";
    private static final String TOTAL_UNDELETED_USERS = "\r\nTotal Undeleted Users : ";
    private static final String IS_SIMULATION_MODE = "\r\nIs Simulation Mode : ";
    private static final String BATCH_SIZE = "\r\nBatch Size : ";
    private static final String REQUEST_LIMIT = "\r\nRequest Limit : ";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("PMD.LawOfDemeter")
    public void logSummary(long startTime, long endTime, int processedUsers, int deletedUsers, int failedDeletions) {
        StringBuilder stringBuilder = new StringBuilder(SUMMARY_HEADING_STRING);


        stringBuilder
            .append(CR_STRING)
            .append(DOTTED_LINE)
            .append(String.format(FORMAT_STR_LENGTH_30,DISPOSER_START_TIME))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10,dateFormat.format(new Date(startTime))))
            .append(String.format(FORMAT_STR_LENGTH_30,DISPOSER_END_TIME))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10,dateFormat.format(new Date(endTime))))
            .append(String.format(FORMAT_STR_LENGTH_30,DISPOSER_EXECUTION_TIME))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10, getDurationFromLong(endTime - startTime)))
            .append(String.format(FORMAT_STR_LENGTH_30,TOTAL_PROCESSED_USERS))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10,processedUsers))

            .append(String.format(FORMAT_STR_LENGTH_30, TOTAL_DELETED_USERS))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10, deletedUsers - failedDeletions))

            .append(String.format(FORMAT_STR_LENGTH_30, TOTAL_ERRORED_DELETIONS))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10, failedDeletions))

            .append(String.format(FORMAT_STR_LENGTH_30,TOTAL_UNDELETED_USERS))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10,processedUsers - deletedUsers))
            .append(String.format(FORMAT_STR_LENGTH_30,IS_SIMULATION_MODE))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10,parameterResolver.getIsSimulation()))
            .append(String.format(FORMAT_STR_LENGTH_30,BATCH_SIZE))
            .append(TAB_STRING)
            .append(String.format(FORMAT_STR_LENGTH_10,parameterResolver.getBatchSize()))
            .append(String.format(FORMAT_STR_LENGTH_30,REQUEST_LIMIT))
            .append(TAB_STRING)
            .append(parameterResolver.getRequestLimit())
            .append(CR_STRING)
            .append(DOTTED_LINE);
        log.info(stringBuilder.toString());
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

        final Map<String, Object> valueMappings = new ConcurrentHashMap<>();
        final long executionTime  =  summary.getEndTime() - summary.getStartTime();
        valueMappings.put("startTime", dateFormat.format(new Date(summary.getStartTime())));
        valueMappings.put("endTime", dateFormat.format(new Date(summary.getEndTime())));
        valueMappings.put("totalTime", getDurationFromLong(executionTime));
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

}
