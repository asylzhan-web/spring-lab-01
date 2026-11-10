package kz.iitu.springlab.security;

import kz.iitu.springlab.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Task 2.4: what is stored in password_hash, and what is not
@SpringBootTest
class PasswordStorageTest extends PostgresIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder passwordEncoder;

    Map<String, String> storedHashes() {
        return jdbc.queryForList("select username, password_hash from app_user").stream()
                .collect(Collectors.toMap(r -> (String) r.get("username"), r -> (String) r.get("password_hash")));
    }

    @Test
    void theTableHoldsBcryptHashesAndNoPlaintext() {
        Map<String, String> hashes = storedHashes();

        assertThat(hashes).containsOnlyKeys("user", "admin");
        hashes.values().forEach(hash -> {
            // {bcrypt} (added by V6 for variant 4) + 60 characters: $2a$ + cost + 22 of salt + 31 of hash
            assertThat(hash).matches("\\{bcrypt}\\$2a\\$10\\$[./A-Za-z0-9]{53}");
            assertThat(hash).doesNotContain("user123").doesNotContain("admin123");
        });
    }

    @Test
    void theStoredHashesVerifyOnlyTheirOwnPassword() {
        Map<String, String> hashes = storedHashes();

        assertThat(passwordEncoder.matches("user123", hashes.get("user"))).isTrue();
        assertThat(passwordEncoder.matches("admin123", hashes.get("admin"))).isTrue();
        assertThat(passwordEncoder.matches("admin123", hashes.get("user"))).isFalse();
    }

    @Test
    void theSamePasswordGivesADifferentHashEveryTime() {
        // Task 2.2: "run it twice and compare the two outputs for the same password"
        var encoder = new BCryptPasswordEncoder();
        String first = encoder.encode("user123");
        String second = encoder.encode("user123");

        System.out.println("encode(\"user123\") #1: " + first);
        System.out.println("encode(\"user123\") #2: " + second);

        assertThat(first).isNotEqualTo(second);                    // a new random salt every time
        assertThat(first.substring(0, 7)).isEqualTo("$2a$10$");    // version 2a, cost 10 = 2^10 rounds
        assertThat(encoder.matches("user123", first)).isTrue();    // the salt is inside the hash
        assertThat(encoder.matches("user123", second)).isTrue();
    }
}
