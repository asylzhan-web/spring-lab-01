-- The catalogue that used to live in an in-memory map (labs 1-5) now lives in PostgreSQL.
-- The schema is owned by Flyway: Hibernate only validates it (spring.jpa.hibernate.ddl-auto=validate).
create table book (
    id             bigserial    primary key,
    title          varchar(200) not null,
    author         varchar(120),
    genre          varchar(20)  not null,
    published_year integer      not null check (published_year between 1 and 2100)
);
