USE CoreDB;
CREATE TABLE user (
    username VARCHAR(20) PRIMARY KEY,
    password VARCHAR(64) NOT NULL
);