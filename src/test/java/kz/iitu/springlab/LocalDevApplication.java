package kz.iitu.springlab;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs the application on a computer without PostgreSQL or Docker:
 * <pre>
 *     ./mvnw spring-boot:test-run          (Windows: mvnw.cmd spring-boot:test-run)
 * </pre>
 * An embedded PostgreSQL 17 is started first, with the database {@code springlab} on
 * localhost:5432 (user {@code postgres}, any password), so a database client can connect to it
 * while the application runs. The database is created empty on every start and Flyway migrates it.
 * The port can be changed with {@code -Dspringlab.pg.port=5433}.
 * <p>
 * With a real PostgreSQL, run {@link SpringLab01Application} as usual instead.
 */
public final class LocalDevApplication {

    private LocalDevApplication() {
    }

    public static void main(String[] args) throws IOException, SQLException {
        int port = Integer.getInteger("springlab.pg.port", 5432);
        EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setPort(port)
                .setServerConfig("lc_messages", "C")
                .start();                                   // stopped by a JVM shutdown hook
        try (Connection connection = postgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("create database springlab");
        }

        System.setProperty("spring.datasource.url", postgres.getJdbcUrl("postgres", "springlab"));
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "postgres");
        // a DevTools restart would call main() again and try to start a second database on the same port
        System.setProperty("spring.devtools.restart.enabled", "false");

        SpringApplication.run(SpringLab01Application.class, args);
    }
}
