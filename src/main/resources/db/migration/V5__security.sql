-- Lab 10, task 2: users and roles live in the database, passwords are stored as BCrypt hashes.
-- A new migration file; applied migrations are never edited.
create table app_user (
    id            bigserial primary key,
    username      varchar(60)  not null unique,
    password_hash varchar(100) not null,     -- BCrypt is 60 characters
    enabled       boolean      not null default true
);

create table user_role (
    user_id bigint      not null references app_user (id),
    role    varchar(20) not null,
    primary key (user_id, role)
);

-- two users; the hashes are of user123 and admin123
-- (generated with new BCryptPasswordEncoder().encode(...), see logs/lab10/04_task2_bcrypt_hashes.txt)
insert into app_user (username, password_hash) values
    ('user',  '$2a$10$bYP3SX7/zuYHIMGrFOo.f.CNI0FICH6AXZg43r/KrINqvkk3m7yoW'),
    ('admin', '$2a$10$yMlcd1xOQr3VnetjpNWD5.BOKdQUnIf.KX3u6I.u8aPmxBk43wGTe');

insert into user_role (user_id, role)
    select id, 'USER'  from app_user;
insert into user_role (user_id, role)
    select id, 'ADMIN' from app_user where username = 'admin';
