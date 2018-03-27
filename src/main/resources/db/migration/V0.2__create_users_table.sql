CREATE TABLE IF NOT EXISTS users (
  uuid UUID NOT NULL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  username VARCHAR(255),
  firstname VARCHAR(255),
  lastname VARCHAR(255),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT current_timestamp,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT current_timestamp
);