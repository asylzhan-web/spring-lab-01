package kz.iitu.springlab.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kz.iitu.springlab.PostgresIntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Task 3.4: every cell of the access matrix. The users are the real rows of app_user (@WithUserDetails
// loads them through DatabaseUserDetailsService), so the roles come from user_role, not from the test.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional   // the POSTs below are rolled back after each test
class AccessMatrixTest extends PostgresIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @PersistenceContext
    EntityManager entityManager;

    static MockHttpServletRequestBuilder createBook(String title) {
        return post("/books")
                .param("title", title)
                .param("genre", "NOVEL")
                .param("year", "2020");
    }

    int booksTitled(String title) {
        return jdbc.queryForObject("select count(*) from book where title = ?", Integer.class, title);
    }

    @Nested
    class Anonymous {

        @Test
        void startPageIsOpen() throws Exception {
            mvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Not logged in")));
        }

        @Test
        void booksRedirectToTheLoginPage() throws Exception {
            mvc.perform(get("/books"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        void createWithAValidTokenRedirectsToTheLoginPage() throws Exception {
            mvc.perform(createBook("Anonymous").with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/login"));
            assertThat(booksTitled("Anonymous")).isZero();
        }

        @Test
        void createWithoutATokenIsRefusedByCsrfBeforeAnyRoleCheck() throws Exception {
            mvc.perform(createBook("Anonymous"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void adminRedirectsToTheLoginPage() throws Exception {
            mvc.perform(get("/admin"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        void aUrlWithNoRuleRedirectsToTheLoginPage() throws Exception {
            mvc.perform(get("/nowhere"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        void loginPageAndStylesheetAreOpen() throws Exception {
            mvc.perform(get("/login")).andExpect(status().isOk());
            mvc.perform(get("/css/app.css")).andExpect(status().isOk());
        }
    }

    @Nested
    @WithUserDetails("user")
    class AsUser {

        @Test
        void startPageShowsWhoIsLoggedIn() throws Exception {
            mvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Logged in as <b>user</b>")))
                    .andExpect(content().string(containsString("[ROLE_USER]")));
        }

        @Test
        void booksAreOpenButTheAdminControlsAreHidden() throws Exception {
            mvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Effective Java")))
                    .andExpect(content().string(not(containsString("Add a book"))))
                    .andExpect(content().string(not(containsString("Administration"))));
        }

        @Test
        void createIsForbidden() throws Exception {
            mvc.perform(createBook("By user").with(csrf()))
                    .andExpect(status().isForbidden());
            assertThat(booksTitled("By user")).isZero();
        }

        @Test
        void deleteIsForbidden() throws Exception {
            mvc.perform(post("/books/1/delete").with(csrf()))
                    .andExpect(status().isForbidden());
            assertThat(jdbc.queryForObject("select count(*) from book where id = 1", Integer.class)).isOne();
        }

        @Test
        void adminIsForbidden() throws Exception {
            mvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void aUrlWithNoRuleIsForbidden() throws Exception {
            mvc.perform(get("/nowhere")).andExpect(status().isForbidden());
            mvc.perform(get("/api/books")).andExpect(status().isForbidden());
        }
    }

    @Nested
    @WithUserDetails("admin")
    class AsAdmin {

        @Test
        void startPageShowsTheAdministrationLink() throws Exception {
            mvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Logged in as <b>admin</b>")))
                    .andExpect(content().string(containsString("[ROLE_ADMIN, ROLE_USER]")))
                    .andExpect(content().string(containsString("href=\"/admin\"")));
        }

        @Test
        void booksShowTheAdminControls() throws Exception {
            mvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Add a book")));
        }

        @Test
        void createIsAllowed() throws Exception {
            mvc.perform(createBook("Genuine").with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/books"));
            assertThat(booksTitled("Genuine")).isOne();
        }

        @Test
        void deleteIsAllowed() throws Exception {
            mvc.perform(post("/books/1/delete").with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/books"));
            entityManager.flush();   // the test transaction is never committed, so push the DELETE to the database
            assertThat(jdbc.queryForObject("select count(*) from book where id = 1", Integer.class)).isZero();
        }

        @Test
        void adminListsTheUsersWithoutTheirHashes() throws Exception {
            mvc.perform(get("/admin"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<td>admin</td>")))
                    .andExpect(content().string(containsString("<td>user</td>")))
                    .andExpect(content().string(containsString("BCrypt $2a$10$ (cost 10)")))
                    .andExpect(content().string(not(containsString(
                            jdbc.queryForObject("select password_hash from app_user where username = 'user'",
                                    String.class)))));
        }

        @Test
        void aUrlWithNoRuleIsForbiddenEvenForTheAdministrator() throws Exception {
            mvc.perform(get("/nowhere")).andExpect(status().isForbidden());
        }
    }
}
