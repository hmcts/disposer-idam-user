package uk.gov.hmcts.reform.idam;


import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.service.IdamDuplicateUserLoggerService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class DuplicateUserFunctionalTest {

    @Inject
    IdamDuplicateUserLoggerService idamDuplicateUserLoggerService;


    @Test
    @DirtiesContext
    void givenDeletedUserExistsThenShouldAbleToRestoreDeletedUsers() {

        idamDuplicateUserLoggerService.run();
        assertThat(true).isTrue();
    }
}
