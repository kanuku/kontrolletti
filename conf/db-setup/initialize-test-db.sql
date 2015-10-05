DROP DATABASE IF EXISTS kontrolletti_test;
DROP ROLE IF EXISTS kontrolletti_test;
CREATE ROLE kontrolletti_test WITH LOGIN PASSWORD 'kontrolletti_test';
CREATE DATABASE kontrolletti_test;
GRANT ALL PRIVILEGES ON DATABASE "kontrolletti_test" to kontrolletti_test;
\connect kontrolletti_test;
CREATE SCHEMA kont_data;
GRANT ALL PRIVILEGES ON SCHEMA "kont_data" to kontrolletti_test;