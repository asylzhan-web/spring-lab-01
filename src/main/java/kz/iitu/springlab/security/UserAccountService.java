package kz.iitu.springlab.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

// The administrator's view of app_user. Lives outside kz.iitu.springlab.service on purpose:
// the logging aspects of lab 4 print the arguments of every method in that package.
@Service
@Transactional(readOnly = true)
public class UserAccountService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAccount> findAll() {
        return users.findAllByOrderByUsernameAsc().stream()
                .map(UserAccount::of)
                .toList();
    }

    // Variant 4: a password set now is hashed by the current encoder, i.e. Argon2 ({argon2}...)
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserAccount create(String username, String rawPassword, boolean administrator) {
        if (users.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        }
        Set<String> roles = administrator ? Set.of("USER", "ADMIN") : Set.of("USER");
        AppUser saved = users.save(new AppUser(username, passwordEncoder.encode(rawPassword), roles));
        return UserAccount.of(saved);
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

    public static class UsernameTakenException extends RuntimeException {

        public UsernameTakenException(String username) {
            super("A user named «" + username + "» already exists");
        }
    }
}
