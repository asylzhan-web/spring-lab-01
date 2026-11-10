package kz.iitu.springlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringLab01ApplicationTests extends PostgresIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayOwnsTheSchema() {
        assertThat(jdbc.queryForList("select version from flyway_schema_history where success order by installed_rank",
                String.class))
                .containsExactly("1", "2", "5");
        assertThat(jdbc.queryForObject("select count(*) from book", Integer.class)).isEqualTo(5);
    }
}
