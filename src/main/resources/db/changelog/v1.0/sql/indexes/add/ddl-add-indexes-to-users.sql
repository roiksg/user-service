CREATE UNIQUE INDEX idx_users_email_unique ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_name_surname ON users(name, surname);