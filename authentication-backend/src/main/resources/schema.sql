CREATE TABLE users (
  username VARCHAR_IGNORECASE(50) NOT NULL PRIMARY KEY,
  password VARCHAR(255)           NOT NULL,
  enabled  BOOLEAN                NOT NULL
);

CREATE TABLE authorities (
  username  VARCHAR_IGNORECASE(50) NOT NULL,
  authority VARCHAR_IGNORECASE(50) NOT NULL,
  CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);

CREATE UNIQUE INDEX ix_auth_username
  ON authorities (username, authority);

CREATE TABLE audit (
  event_id        INT AUTO_INCREMENT     NOT NULL PRIMARY KEY,
  username        VARCHAR_IGNORECASE(50) NOT NULL,
  event_type      VARCHAR_IGNORECASE(50) NOT NULL,
  client_id       VARCHAR_IGNORECASE(50) NOT NULL,
  grant_type      VARCHAR_IGNORECASE(50) NOT NULL,
  event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_username_event_type
  ON audit (username, event_type);
