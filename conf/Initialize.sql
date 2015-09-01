DROP DATABASE IF EXISTS kontrolletti;
DROP ROLE IF EXISTS kontrolletti;
CREATE ROLE kontrolletti WITH LOGIN PASSWORD 'kontrolletti';
CREATE DATABASE kontrolletti;
GRANT ALL PRIVILEGES ON DATABASE "kontrolletti" to kontrolletti;
CREATE SCHEMA kontrolletti_data;
GRANT ALL PRIVILEGES ON SCHEMA "kontrolletti_data" to kontrolletti;