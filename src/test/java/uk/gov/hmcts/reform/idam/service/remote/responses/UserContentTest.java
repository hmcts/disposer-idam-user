package uk.gov.hmcts.reform.idam.service.remote.responses;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.LinguisticNaming")
class UserContentTest {

    @Test
    void getRolesReturnsLowerCasedRoles() {
        var user = new UserContent("001", List.of("CITIZEN", "Claimant", "defendant"));
        assertThat(user.getRoles()).containsAll(List.of("citizen", "claimant", "defendant"));
    }

    @Test
    void getRolesReturnsEmptyListOnNull() {
        var user = new UserContent("001", null);
        assertThat(user.getRoles()).isNotNull().isEmpty();
    }

    @Test
    void getLettersRemovedRolesLeavesAll() {
        var user = new UserContent("001", List.of("CITIZEN", "Claimant", "defendant"));
        assertThat(user.getRoles()).containsAll(List.of("citizen", "claimant", "defendant"));
    }

    @Test
    void getLettersRemovedRolesRemovesLetters() {
        var user = new UserContent("001", List.of("citizen", "letter-1", "LETTER-2", "Letter-3"));
        assertThat(user.getLettersRemovedRoles()).hasSize(1).contains("citizen");
    }

    @Test
    void getLettersRemovedRolesReturnsEmptyOnNull() {
        var user = new UserContent("001", null);
        assertThat(user.getLettersRemovedRoles()).isNotNull().isEmpty();
    }
}
