DROP DATABASE IF EXISTS kontrolletti_DB;
DROP ROLE IF EXISTS kontrolletti;
CREATE ROLE kontrolletti WITH LOGIN PASSWORD 'kontrolletti';
CREATE DATABASE kontrolletti_DB;
GRANT ALL PRIVILEGES ON DATABASE "kontrolletti" to kontrolletti;
CREATE SCHEMA kont_data;
GRANT ALL PRIVILEGES ON SCHEMA "kont_data" to kontrolletti;