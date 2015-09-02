# AppInfos schema


# --- !Ups
 
CREATE TABLE apps ( 
    scm_url varchar(255) NOT NULL,
    spec_url varchar(255),
    fullname varchar(255),
    last_mod varchar(255),
    PRIMARY KEY (scm_url)
);
 
# --- !Downs
 
DROP TABLE apps;