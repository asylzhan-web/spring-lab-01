package kz.iitu.springlab.security;

import kz.iitu.springlab.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Checklist item 9: the scenario a URL rule would miss.
// Somebody opens /api/** to every logged-in user (here: a second filter chain added by this test only).
// The URL rules for /books/** no longer matter — BookRestController reaches BookService.delete directly.
// Only @PreAuthorize on the service still stops an ordinary user.
@SpringBootTest
@AutoConfigureMockMvc
@Import(UrlRuleIsNotEnoughTest.CarelessApiChain.class)
@Transactional
class UrlRuleIsNotEnoughTest extends PostgresIntegrationTest {

    @TestConfiguration
    static class CarelessApiChain {

        @Bean
        @Order(1)
        SecurityFilterChain carelessApi(HttpSecurity http) throws Exception {
            http.securityMatcher("/api/**")
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults());
            return http.build();
        }
    }

    @Autowired
    MockMvc mvc;

    @Test
    void theUserGetsThroughTheUrlRuleButNotThroughTheMethodRule() throws Exception {
        mvc.perform(get("/api/books/1").with(httpBasic("user", "user123")))
                .andExpect(status().isOk());                       // the careless URL rule lets the user in

        mvc.perform(delete("/api/books/1").with(httpBasic("user", "user123")).with(csrf()))
                .andExpect(status().isForbidden());                // @PreAuthorize("hasRole('ADMIN')") refuses

        mvc.perform(get("/api/books/1").with(httpBasic("user", "user123")))
                .andExpect(status().isOk());                       // and the book is still there
    }

    @Test
    void theAdministratorIsAllowed() throws Exception {
        mvc.perform(delete("/api/books/1").with(httpBasic("admin", "admin123")).with(csrf()))
                .andExpect(status().isNoContent());
    }
}
