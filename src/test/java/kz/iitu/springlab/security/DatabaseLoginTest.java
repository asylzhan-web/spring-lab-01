package kz.iitu.springlab.security;

import kz.iitu.springlab.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

// Task 2: the login form checks the password against the BCrypt hash stored in app_user
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DatabaseLoginTest extends PostgresIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void userLogsInWithTheRoleFromUserRole() throws Exception {
        mvc.perform(formLogin().user("user").password("user123"))
                .andExpect(authenticated().withUsername("user").withRoles("USER"))
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    void adminLogsInWithBothRoles() throws Exception {
        mvc.perform(formLogin().user("admin").password("admin123"))
                .andExpect(authenticated().withUsername("admin").withRoles("ADMIN", "USER"));
    }

    @Test
    void aWrongPasswordAndAnUnknownUserGetTheSameAnswer() throws Exception {
        MvcResult wrongPassword = mvc.perform(formLogin().user("user").password("admin123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();
        MvcResult unknownUser = mvc.perform(formLogin().user("nobody").password("user123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        Object first = lastException(wrongPassword);
        Object second = lastException(unknownUser);
        assertThat(first).isInstanceOf(BadCredentialsException.class);
        assertThat(second).isInstanceOf(BadCredentialsException.class);
        assertThat(((Exception) first).getMessage()).isEqualTo(((Exception) second).getMessage());
    }

    @Test
    void aDisabledUserCannotLogIn() throws Exception {
        jdbc.update("update app_user set enabled = false where username = 'user'");   // rolled back afterwards

        MvcResult result = mvc.perform(formLogin().user("user").password("user123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();
        assertThat(lastException(result)).isInstanceOf(DisabledException.class);
    }

    @Test
    @WithUserDetails("user")
    void logoutIsAPostAndReturnsToTheStartPage() throws Exception {
        mvc.perform(post("/logout").with(csrf()))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/?logout"));
    }

    private static Object lastException(MvcResult result) {
        return result.getRequest().getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
