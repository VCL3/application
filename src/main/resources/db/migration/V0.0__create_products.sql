CREATE TYPE clothing_size_enum AS ENUM('XXXS', 'XXS', 'XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL', '4XL');
CREATE TYPE sex_enum AS ENUM('MEN', 'WOMEN', 'UNISEX', 'NOSEX');

CREATE TABLE IF NOT EXISTS products (
  uuid UUID NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  designer VARCHAR(255),
  sex sex_enum,
  available_sizes clothing_size_enum[],
  clothing_category VARCHAR(255),
  original_price JSONB,
  current_price JSONB NOT NULL,
  is_on_sale BOOLEAN,
  sale_discount INTEGER,
  source VARCHAR(255),
  external_link VARCHAR(255) NOT NULL,
  image_links VARCHAR(255)[],
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT current_timestamp,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT current_timestamp
);