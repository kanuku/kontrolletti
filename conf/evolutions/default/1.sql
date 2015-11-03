# --- !Ups

CREATE TABLE kont_data.repositories
(
  url 					CHARACTER 		VARYING NOT NULL PRIMARY KEY,
  host 					CHARACTER 		VARYING NOT NULL,
  project 				CHARACTER 		VARYING NOT NULL,
  repository 			CHARACTER 		VARYING NOT NULL,
  is_synchronizable 	BOOLEAN 				NOT NULL,
  synchronized_at 		TIMESTAMP 		WITH TIME ZONE,
  last_failed_at 		TIMESTAMP 		WITH TIME ZONE,
  links 				JSONB
);

CREATE UNIQUE INDEX CONCURRENTLY hpr_idx on kont_data.repositories (host, project, repository);


CREATE TABLE kont_data.commits
(
  id 					CHARACTER VARYING NOT NULL PRIMARY KEY,
  repository_url 		CHARACTER VARYING NOT NULL,
  date 					TIMESTAMP WITH TIME ZONE NOT NULL,
  nr_tickets integer    NOT NULL,
  json_value 			JSONB NOT NULL, 
  CONSTRAINT repository_url 
  		FOREIGN KEY (repository_url)
      	REFERENCES kont_data.repositories (url) 
);

# --- !Downs
DROP INDEX IF EXISTS kont_data.hpr_idx ;
DROP TABLE IF EXISTS kont_data.commits ;
DROP TABLE IF EXISTS  kont_data.repositories ;
SET search_path TO kont_data,public;