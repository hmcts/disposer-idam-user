package uk.gov.hmcts.reform.idam.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.parameter.ParameterResolver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @SuppressWarnings("PMD.LawOfDemeter")
    public void logSummary(long startTime, long endTime, int processedUsers, int deletedUsers, int failedDeletions) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        StringBuilder stringBuilder = new StringBuilder(SUMMARY_HEADING_STRING);
        long executionTime  =  endTime - startTime;
        long hh = TimeUnit.MILLISECONDS.toHours(executionTime);
        long mm = TimeUnit.MILLISECONDS.toMinutes(executionTime) % 60;
        long ss = TimeUnit.MILLISECONDS.toSeconds(executionTime) % 60;
        String hms = String.format("%02d:%02d:%02d", hh, mm, ss);

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
            .append(String.format(FORMAT_STR_LENGTH_10,hms))
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

}
