package kz.iitu.springlab;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;

// Every integration test runs against a real PostgreSQL 17 started from the test JVM (no Docker needed).
// Flyway migrates it exactly as it migrates the springlab database; Hibernate validates the result.
public abstract class PostgresIntegrationTest {

    private static final EmbeddedPostgres POSTGRES = start();

    private static EmbeddedPostgres start() {
        try {
            return EmbeddedPostgres.builder()
                    .setServerConfig("lc_messages", "C")   // server messages in English
                    .start();                              // stopped by a JVM shutdown hook
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start the embedded PostgreSQL", e);
        }
    }

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }
}
