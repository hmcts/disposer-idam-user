package uk.gov.hmcts.reform.idam.service.remote.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserContent {

    private static final String LETTER_PREFIX = "letter-";
    private String id;
    private List<String> roles;

    public List<String> getRoles() {
        if (this.roles == null) {
            this.roles = List.of();
        }
        return roles.stream().map(String::toLowerCase).toList();
    }

    public List<String> getLettersRemovedRoles() {
        return this.getRoles().stream().filter(role -> !role.toLowerCase().startsWith(LETTER_PREFIX)).toList();
    }
}
