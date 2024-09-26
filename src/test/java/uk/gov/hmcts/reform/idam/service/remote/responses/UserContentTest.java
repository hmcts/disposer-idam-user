package uk.gov.hmcts.reform.idam.service.remote.responses;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserContentTest {

    @Test
    void shouldGetLowercasedRoles() {
        UserContent userContent = new UserContent();
        userContent.setRoles(List.of("A", "B", "C"));
        assertEquals(List.of("a", "b", "c"), userContent.getLowercasedRoles());
    }

    @Test
    void shouldLowercasedRolesReturnsEmptyList() {
        UserContent userContent = new UserContent();
        userContent.setRoles(null);
        assertEquals(List.of(), userContent.getLowercasedRoles());

        userContent.setRoles(List.of());
        assertEquals(List.of(), userContent.getLowercasedRoles());
    }

    @Test
    void filterOutPatternRoles() {
        UserContent userContent = new UserContent();
        userContent.setRoles(List.of("PAT-123", "PAT-234", "PAT-345"));
        assertEquals(List.of("pat-123", "pat-234", "pat-345"), userContent.filterOutPatternRoles(null));
        assertEquals(List.of("pat-234", "pat-345"), userContent.filterOutPatternRoles("pat-1"));
        assertEquals(List.of("pat-123", "pat-234", "pat-345"), userContent.filterOutPatternRoles("something"));
    }
}
