package kz.iitu.springlab.security;

import kz.iitu.springlab.PostgresIntegrationTest;
import kz.iitu.springlab.catalog.BookRequest;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.catalog.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Task 3.2: the rule travels with the method — no controller, no URL, the service refuses by itself
@SpringBootTest
@Transactional
class MethodSecurityTest extends PostgresIntegrationTest {

    @Autowired
    BookService books;

    @Autowired
    UserAccountService accounts;

    @Test
    @WithMockUser(roles = "USER")
    void aUserMayReadButNotDelete() {
        assertThat(books.findAll(null)).hasSize(5);
        assertThatThrownBy(() -> books.delete(1)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> books.create(new BookRequest("X", null, Genre.NOVEL, 2020)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> accounts.findAll()).isInstanceOf(AccessDeniedException.class);
        assertThat(books.findById(1)).isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void anAdministratorMayDelete() {
        assertThat(books.delete(1)).isTrue();
        assertThat(books.findById(1)).isEmpty();
        assertThat(accounts.findAll()).extracting(UserAccountService.UserAccount::username)
                .containsExactly("admin", "user");
    }

    @Test
    void nobodyLoggedInIsNotEvenAsked() {
        assertThatThrownBy(() -> books.delete(1)).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
