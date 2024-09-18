package uk.gov.hmcts.reform.idam.service.remote.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class UserContent {
    private String id;
    private List<String> roles;

    public List<String> getLowercasedRoles() {
        if (this.roles == null) {
            this.roles = List.of();
        }
        return roles.stream().map(String::toLowerCase).toList();
    }

    /**
     * Filters out roles that start with the given pattern.
     * Please note it only checks if it *starts* with the given pattern.
     * @param pattern - the pattern to filter out
     * @return a list of roles that do not start with the given pattern
     */
    public List<String> filterOutPatternRoles(String pattern) {
        if (pattern == null) {
            return this.getLowercasedRoles();
        }
        return this.getLowercasedRoles().stream().filter(role -> !role.toLowerCase().startsWith(pattern)).toList();
    }
}
