package uk.gov.hmcts.reform.idam.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletionLog;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class DeletedAccountsRequest {
    private List<DeletionLog> deletionLogs;
}
