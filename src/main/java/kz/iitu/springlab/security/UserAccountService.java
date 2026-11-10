package kz.iitu.springlab.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// The administrator's view of app_user. Lives outside kz.iitu.springlab.service on purpose:
// the logging aspects of lab 4 print the arguments of every method in that package.
@Service
@Transactional(readOnly = true)
public class UserAccountService {

    private final AppUserRepository users;

    public UserAccountService(AppUserRepository users) {
        this.users = users;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAccount> findAll() {
        return users.findAllByOrderByUsernameAsc().stream()
                .map(UserAccount::of)
                .toList();
    }

    // What the administration page shows: never the hash itself, only how it was made
    public record UserAccount(String username, List<String> roles, boolean enabled, String storedAs) {

        static UserAccount of(AppUser user) {
            return new UserAccount(user.getUsername(),
                    user.getRoles().stream().sorted().toList(),
                    user.isEnabled(),
                    HashScheme.describe(user.getPasswordHash()));
        }
    }
}
