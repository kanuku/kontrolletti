# AppInfos schema


# --- !Ups
 
CREATE TABLE apps ( 
    SCM_URL varchar(255) NOT NULL,
    DOC_URL varchar(255),
    SPEC_URL varchar(255),
    LAST_MODIFIED varchar(255),
    PRIMARY KEY (SCM_URL)
);
 
# --- !Downs
 
DROP TABLE apps;