package uk.gov.hmcts.reform.idam;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.service.IdamUserRestorerService;
import uk.gov.hmcts.reform.idam.util.RestoreSummary;

import java.util.List;

import static org.junit.Assert.assertEquals;


@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
class UserRestoreFunctionalTest {

    @Inject
    IdamUserRestorerService idamUserRestoreService;


    @Test
    void givenDeletedUserExistsThenShouldAbleToRestoreDeletedUsers() {
        idamUserRestoreService.run();
        RestoreSummary restoreSummary = idamUserRestoreService.getSummary();
        List<String> userIds = restoreSummary.getSuccessful();
        if (!userIds.isEmpty()) {
            assertEquals("User has not been restored successfully",1,userIds.size());
        }
    }

}
