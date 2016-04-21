# --- !Ups
ALTER TABLE kont_data.commits ADD COLUMN nr_parents integer NOT NULL DEFAULT 0;
ALTER TABLE kont_data.commits ADD COLUMN is_valid boolean NOT NULL DEFAULT false;

# --- !Downs
ALTER TABLE kont_data.commits DROP COLUMN IF EXISTS nr_parents;
ALTER TABLE kont_data.commits DROP COLUMN IF EXISTS is_valid;
