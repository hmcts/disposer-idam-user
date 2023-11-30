package uk.gov.hmcts.reform.idam.service.remote.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeletedUsersResponse {
    @JsonProperty("deletionLogs")
    private List<DeletionLog> deletionLogs;

    @JsonProperty("startRecordNumber")
    private int startRecordNumber;

    @JsonProperty("moreRecords")
    private boolean moreRecords;
}
