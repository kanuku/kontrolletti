DROP DATABASE IF EXISTS kontrolletti;
DROP ROLE IF EXISTS kontrolletti;
CREATE ROLE kontrolletti WITH LOGIN PASSWORD 'kontrolletti';
CREATE DATABASE kontrolletti;
GRANT ALL PRIVILEGES ON DATABASE "kontrolletti" to kontrolletti;
\connect kontrolletti;
CREATE SCHEMA kont_data;
GRANT ALL PRIVILEGES ON SCHEMA "kont_data" to kontrolletti;