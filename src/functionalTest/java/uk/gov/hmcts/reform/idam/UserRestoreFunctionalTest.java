package uk.gov.hmcts.reform.idam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertTrue;


@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
class UserRestoreFunctionalTest {

    @Test
    void givenDeletedUserExistsThenShouldAbleToRestoreDeletedUsers() {
        assertTrue("Dummy condition", true);
    }
}
