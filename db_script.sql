CREATE TABLE apparels ( 
  id BIGINT, 
  category VARCHAR(100), 
  sub_category VARCHAR(50), 
  uri VARCHAR(200), 
  gsutil_uri VARCHAR(200),
  image VARCHAR(100), 
  content VARCHAR(2000), 
  pdt_desc VARCHAR(5000), 
  color VARCHAR(2000),
  gender VARCHAR(200),
  embedding vector(768),
  img_embeddings vector(1408),
  additional_specification VARCHAR(100000));


INSERT QUERIES FROM FILE >> https://docs.google.com/spreadsheets/d/1WhaeQSIyXgRkx41koQjwb001kIIsgceVhptUezRtjbw/edit?usp=sharing

  Copy the insert query statements from the insert scripts sql in the sheet linked above. 
  You can copy 10-50 insert statements for a quick demo of this use case. 
  There is a selected list of inserts here in this “Selected Inserts 25-30 rows” tab.
  
  >> Source of data and license: https://www.kaggle.com/datasets/vikashrajluhaniwal/fashion-images/
  
  


UPDATE apparels SET embedding = embedding('text-embedding-005',pdt_desc)::vector 
WHERE pdt_desc IS NOT NULL;

update apparels set img_embeddings = ai.image_embedding(
  model_id => 'multimodalembedding@001',
  image => gsutil_uri,
  mimetype => 'image/jpg')       
where gsutil_uri is not null;

CREATE INDEX idx_category ON apparels (category);
CREATE INDEX idx_sub_category ON apparels (sub_category);
CREATE INDEX idx_color ON apparels (color);
CREATE INDEX idx_gender ON apparels (gender); 

SET scann.enable_inline_filtering = on;
SET scann.enable_preview_features = on; --(you cannot do this without restarting the instace, so better do it from the instance config console)


CREATE EXTENSION IF NOT EXISTS alloydb_scann;

CREATE INDEX apparels_index ON apparels 
USING scann (embedding cosine)
WITH (num_leaves=32);

CREATE INDEX apparels_img_index ON apparels 
USING scann (img_embeddings cosine)
WITH (num_leaves=32);



