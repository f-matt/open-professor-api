CREATE DATABASE open_professor WITH OWNER open_professor;

CREATE EXTENSION pgcrypto;

CREATE SCHEMA auth;

CREATE TABLE auth.permissions (
    id SERIAL,
    description VARCHAR(20) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE (description)
);

CREATE TABLE auth.roles (
    id SERIAL,
    description VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE (description)
);

CREATE TABLE auth.roles_permissions (
    id SERIAL,
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (role_id) REFERENCES auth.roles (id),
    FOREIGN KEY (permission_id) REFERENCES auth.permissions (id),
    UNIQUE (role_id, permission_id)
);

CREATE TABLE auth.users (
    id SERIAL,
    name VARCHAR (50) NOT NULL,
    username VARCHAR(20) NOT NULL,
    password VARCHAR(256) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE auth.users_roles (
    id SERIAL,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES auth.users (id),
    FOREIGN KEY (role_id) REFERENCES auth.roles (id),
    UNIQUE (user_id, role_id)
);

INSERT INTO auth.permissions (description) VALUES ('ADMIN');

INSERT INTO auth.roles (description) VALUES ('ADMIN');

INSERT INTO auth.roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth.roles r, auth.permissions p
WHERE r.description = 'ADMIN' AND p.description = 'ADMIN';

INSERT INTO auth.users (name, username, password) VALUES ('ADMINISTRATOR', 'admin', ENCODE(DIGEST('<your admin password here>', 'sha256'), 'base64'));

INSERT INTO auth.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth.users u, auth.roles r
WHERE u.username = 'admin' AND r.description = 'ADMIN';


--
-- public schema
--
CREATE TABLE courses (
    id SERIAL,
    name VARCHAR (200) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE questions (
    id SERIAL,
    text TEXT NOT NULL,
    section INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE answers (
    id SERIAL,
    text VARCHAR (200) NOT NULL,
    correct BOOLEAN NOT NULL,
    question_id INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (question_id) REFERENCES questions (id)
);

CREATE TABLE parameters (
    id SERIAL,
    name VARCHAR (20) NOT NULL,
    value text,
    PRIMARY KEY (id)
);
