# --- !Ups

create table students (
  "id" serial  primary key,
  "name" varchar not null,
  "age" int not null
);

# --- !Downs

drop table "students" if exists;
