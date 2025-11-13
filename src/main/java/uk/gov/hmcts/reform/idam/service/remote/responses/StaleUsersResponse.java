package uk.gov.hmcts.reform.idam.service.remote.responses;

import java.util.List;

public record StaleUsersResponse(List<UserContent> content, boolean last) {

    public boolean isLast() {
        return last;
    }
}
