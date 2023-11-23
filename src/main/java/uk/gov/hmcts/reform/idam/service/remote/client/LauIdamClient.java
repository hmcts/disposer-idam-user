package uk.gov.hmcts.reform.idam.service.remote.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.service.remote.responses.DeletedUsersResponse;
import uk.gov.hmcts.reform.idam.util.Constants;

import java.util.Map;

@FeignClient(name = "lauClient", url = "${lau.api.url}")
public interface LauIdamClient {

    @GetMapping(value = Constants.LAU_GET_DELETED_USERS_PATH, consumes = "application/json")
    DeletedUsersResponse getDeletedUsers(
        @RequestHeader Map<String, String> headers,
        @RequestParam(name = "size") int batchSize
    );

    @DeleteMapping(value = Constants.LAU_DELETE_LOG_ENTRY_PATH, consumes = "application/json")
    Response deleteLogEntry(
        @RequestHeader Map<String, String> headers,
        @RequestParam String userId
    );
}
