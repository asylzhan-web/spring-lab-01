package kz.iitu.springlab.security;

import kz.iitu.springlab.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Task 4: the hidden _csrf field is on every POST form, and a POST without it is refused
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CsrfTest extends PostgresIntegrationTest {

    private static final Pattern HIDDEN_CSRF = Pattern.compile("<input type=\"hidden\" name=\"_csrf\" value=\"[^\"]+\"/>");

    @Autowired
    MockMvc mvc;

    @Test
    void theLoginFormCarriesTheToken() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void aLoginPostWithoutTheTokenIsRefused() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", "admin123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin")
    void everyPostFormOnTheCataloguePageCarriesTheToken() throws Exception {
        String html = mvc.perform(get("/books")).andReturn().getResponse().getContentAsString();

        int forms = count(Pattern.compile("<form[^>]*method=\"post\""), html);
        assertThat(forms).isEqualTo(5 + 1 + 1);   // a Delete form per book, the Add form, the Log out form
        assertThat(count(HIDDEN_CSRF, html)).isEqualTo(forms);
    }

    @Test
    @WithUserDetails("admin")
    void aPostWithoutTheTokenIsRefusedEvenForTheAdministrator() throws Exception {
        mvc.perform(post("/books").param("title", "Forged").param("genre", "NOVEL").param("year", "2020"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin")
    void aPostWithAWrongTokenIsRefused() throws Exception {
        mvc.perform(post("/books").param("title", "Forged").param("genre", "NOVEL").param("year", "2020")
                        .with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin")
    void theSameRequestWithTheTokenIsAccepted() throws Exception {
        mvc.perform(post("/books").param("title", "Genuine").param("genre", "NOVEL").param("year", "2020")
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/books"));
    }

    private static int count(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }
}
