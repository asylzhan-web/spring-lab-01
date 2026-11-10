-- Lab 10, individual variant 4: Argon2 instead of BCrypt.
-- The application now uses a DelegatingPasswordEncoder: new passwords are written as {argon2}...,
-- and every stored hash must name the encoder that can verify it. V5 is already applied and is
-- never edited, so the change is this new migration.

-- 1. An Argon2id hash with the Spring Security 5.8 defaults is 97 characters, plus the 8-character
--    {argon2} prefix = 105: the 100 characters of V5 are no longer enough.
alter table app_user alter column password_hash type varchar(255);

-- 2. The existing BCrypt hashes keep working: they are tagged with the id of the encoder that verifies them.
update app_user
   set password_hash = '{bcrypt}' || password_hash
 where password_hash like '$2a$%' or password_hash like '$2b$%' or password_hash like '$2y$%';
