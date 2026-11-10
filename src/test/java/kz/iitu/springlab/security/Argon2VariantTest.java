package kz.iitu.springlab.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kz.iitu.springlab.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Individual variant 4: Argon2 instead of BCrypt; old BCrypt hashes still verify through their {id} prefix
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Argon2VariantTest extends PostgresIntegrationTest {

    private static final String ARGON2 = "\\{argon2}\\$argon2id\\$v=19\\$m=16384,t=2,p=1\\$[A-Za-z0-9+/]{22}\\$[A-Za-z0-9+/]{43}";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PersistenceContext
    EntityManager entityManager;

    String storedHash(String username) {
        return jdbc.queryForObject("select password_hash from app_user where username = ?", String.class, username);
    }

    @Test
    void newPasswordsAreHashedWithArgon2() {
        String first = passwordEncoder.encode("user123");
        String second = passwordEncoder.encode("user123");

        System.out.println("encode(\"user123\") #1: " + first + "  (" + first.length() + " characters)");
        System.out.println("encode(\"user123\") #2: " + second);

        assertThat(first).matches(ARGON2).isNotEqualTo(second);   // a fresh 16-byte salt every time
        assertThat(passwordEncoder.matches("user123", first)).isTrue();
        assertThat(passwordEncoder.matches("user123", second)).isTrue();
    }

    @Test
    void theOldBcryptHashesStillVerifyThroughTheirPrefix() {
        assertThat(storedHash("user")).startsWith("{bcrypt}$2a$10$");
        assertThat(passwordEncoder.matches("user123", storedHash("user"))).isTrue();
        assertThat(passwordEncoder.matches("admin123", storedHash("admin"))).isTrue();
        assertThat(passwordEncoder.matches("wrong", storedHash("admin"))).isFalse();
    }

    @Test
    void aHashWithoutAPrefixIsRejected() {
        String bare = new BCryptPasswordEncoder().encode("user123");   // what V5 stored, before V6

        assertThatThrownBy(() -> passwordEncoder.matches("user123", bare))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("each password must have a password encoding prefix");
    }

    @Test
    void theOldUsersStillLogInThroughTheForm() throws Exception {
        mvc.perform(formLogin().user("user").password("user123"))
                .andExpect(authenticated().withUsername("user").withRoles("USER"));
        mvc.perform(formLogin().user("admin").password("admin123"))
                .andExpect(authenticated().withUsername("admin").withRoles("ADMIN", "USER"));
    }

    @Test
    @WithUserDetails("admin")
    void aUserCreatedByTheAdministratorGetsAnArgon2HashAndCanLogIn() throws Exception {
        mvc.perform(post("/admin/users").with(csrf())
                        .param("username", "librarian")
                        .param("password", "librarian123"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attribute("message", containsString("{argon2} Argon2 $argon2id$v=19$m=16384,t=2,p=1$")));

        entityManager.flush();   // the test transaction is never committed: push the user_role rows to the database
        assertThat(storedHash("librarian")).matches(ARGON2);
        assertThat(jdbc.queryForList("select role from user_role r join app_user u on u.id = r.user_id where username = 'librarian'",
                String.class)).containsExactly("USER");

        TestSecurityContextHolder.clearContext();   // log in as the new user, not on top of the admin's session
        mvc.perform(formLogin().user("librarian").password("librarian123"))
                .andExpect(authenticated().withUsername("librarian").withRoles("USER"));
    }

    @Test
    @WithUserDetails("admin")
    void theAdministrationPageShowsBothSchemes() throws Exception {
        mvc.perform(post("/admin/users").with(csrf())
                .param("username", "chief").param("password", "chief12345").param("administrator", "true"));

        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{bcrypt} BCrypt $2a$10$ (cost 10)")))
                .andExpect(content().string(containsString("{argon2} Argon2 $argon2id$v=19$m=16384,t=2,p=1$")))
                .andExpect(content().string(containsString("ADMIN, USER")));
    }

    @Test
    @WithUserDetails("admin")
    void aDuplicateUsernameIsRejected() throws Exception {
        mvc.perform(post("/admin/users").with(csrf()).param("username", "user").param("password", "another1"))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attribute("error", "A user named «user» already exists"));
        assertThat(storedHash("user")).startsWith("{bcrypt}");   // unchanged
    }

    @Test
    @WithUserDetails("user")
    void onlyTheAdministratorMayCreateUsers() throws Exception {
        mvc.perform(post("/admin/users").with(csrf()).param("username", "intruder").param("password", "intruder1"))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("select count(*) from app_user where username = 'intruder'", Integer.class)).isZero();
    }
}
