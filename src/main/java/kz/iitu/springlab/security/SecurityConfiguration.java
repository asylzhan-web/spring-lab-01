package kz.iitu.springlab.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    // Rules are evaluated in order and the first match wins: specific patterns first, denyAll() last,
    // so that an endpoint nobody wrote a rule for is closed rather than open.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // The container's own ERROR dispatch (rendering a 400/403/404 page) is not a new request:
                // without this line every error page would itself be refused by denyAll().
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/login", "/css/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/books/**").hasRole("ADMIN")   // create, edit, delete
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(form -> form
                // Always land on the catalogue: otherwise a browser's background request made while the
                // login page was open (DevTools, for example) could be "remembered" and replayed after login.
                .defaultSuccessUrl("/books", true)
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/?logout"));
        // CSRF protection is on by default and is deliberately not touched.
        return http.build();
    }

    // Answers «is this the right password?». BCrypt: a per-user random salt and a work factor (10).
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
