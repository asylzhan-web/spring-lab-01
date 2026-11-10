# spring-lab-01

RWPSF 3305 — Web Application Development with Spring Framework.
Bekzhigit Asylzhan, IT1-2411 IS. One branch per laboratory work (`lab01` … `lab05`, `lab10`).

## Lab 10 — the security subsystem (variant 4: Argon2 instead of BCrypt)

| What | Where |
|---|---|
| `SecurityFilterChain`, `PasswordEncoder` | `src/main/java/kz/iitu/springlab/security/SecurityConfiguration.java` |
| `@EnableMethodSecurity` | `security/MethodSecurityConfiguration.java` |
| `UserDetailsService` over `app_user` / `user_role` | `security/DatabaseUserDetailsService.java`, `security/AppUser.java` |
| `@PreAuthorize("hasRole('ADMIN')")` | `catalog/BookService.java` (create, replace, delete), `security/UserAccountService.java` |
| Administrator area | `web/AdminController.java`, `templates/admin/users.html` |
| Who is logged in (`sec:authorize`, `sec:authentication`) | `templates/fragments/header.html` |
| Migrations (Flyway owns the schema) | `src/main/resources/db/migration/V1`, `V2` (catalogue), `V5__security.sql`, `V6__password_encoder_ids.sql` |

Access rules:

| Request | Anonymous | `user` | `admin` |
|---|---|---|---|
| `GET /` | 200 | 200 | 200 |
| `GET /books` | 302 → `/login` | 200 | 200 |
| `POST /books` (create) | 302 → `/login` (403 without a CSRF token) | 403 | 302 → `/books` |
| `GET /admin` | 302 → `/login` | 403 | 200 |
| `GET /nowhere` (no rule) | 302 → `/login` | 403 | 403 |

Test users (teaching passwords only): `user` / `user123` (USER), `admin` / `admin123` (USER, ADMIN).
Their hashes were made with BCrypt (V5); since variant 4 new passwords are hashed with Argon2id and stored as
`{argon2}…`, while the old ones are stored as `{bcrypt}…` (V6) and still verify.

## Running

**With an installed PostgreSQL 16+** — create the database once, then run `SpringLab01Application`:

```
createdb -U postgres springlab
./mvnw spring-boot:run
```

The connection is `jdbc:postgresql://localhost:5432/springlab`, user `postgres`, password `postgres` by default.
Real values belong in environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) or in
`./config/application.yml`, which is git-ignored.

**With Docker** — `docker compose up -d` starts PostgreSQL 17 with the `springlab` database, then run as above.

**Without PostgreSQL or Docker** — an embedded PostgreSQL 17 is started for you:

```
./mvnw spring-boot:test-run          (Windows: mvnw.cmd spring-boot:test-run)
```

It listens on `localhost:5432` (database `springlab`, user `postgres`, any password) while the application runs,
so a database client can connect to it. It starts empty every time and Flyway migrates it.
Stop the application with Ctrl+C (or the Stop button in IDEA) so that the database is stopped too.

Open http://localhost:8080/.

## Tests

```
./mvnw test
```

48 integration tests run against an embedded PostgreSQL 17 (no Docker needed): the access matrix,
login against the database, BCrypt/Argon2 hashes, CSRF, `@PreAuthorize`, and a scenario where a URL rule
alone would not have been enough (`UrlRuleIsNotEnoughTest`).
