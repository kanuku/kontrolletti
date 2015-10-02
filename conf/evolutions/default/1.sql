# --- !Ups

CREATE TABLE kont_data."REPOSITORIES"
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

CREATE TABLE kont_data."COMMITS"
(
  id 					CHARACTER VARYING NOT NULL PRIMARY KEY,
  repository_url 		CHARACTER VARYING NOT NULL,
  parent_ids 			TEXT[],
  date 					TIMESTAMP WITH TIME ZONE NOT NULL,
  json_value 			JSONB NOT NULL, 
  CONSTRAINT repository_url 
  		FOREIGN KEY (repository_url)
      	REFERENCES kont_data."REPOSITORIES" (url) 
);

# --- !Downs
DROP TABLE IF EXISTS kont_data."COMMITS" ;
DROP TABLE IF EXISTS  kont_data."REPOSITORIES" ;
SET search_path TO kont_data,public;