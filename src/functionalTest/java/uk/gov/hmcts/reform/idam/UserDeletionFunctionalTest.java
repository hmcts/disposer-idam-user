package uk.gov.hmcts.reform.idam;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.helpers.IdamUserDataProvider;
import uk.gov.hmcts.reform.idam.service.IdamUserDisposerService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("functional")
@RequiredArgsConstructor
@Slf4j
class UserDeletionFunctionalTest {

    @Inject
    private IdamUserDataProvider idamUserDataProvider;

    @Inject
    private IdamUserDisposerService userDisposerService;

    @BeforeEach
    public void setup() {
        idamUserDataProvider.setup();
    }

    @Test
    void givenARetiredStaleUserExistsThenDisposerShouldBeAbleToDeleteThatUser() {
        List<String> deletedStaleUsers = userDisposerService.run();
        assertThat(deletedStaleUsers).hasSize(1);
    }

}
