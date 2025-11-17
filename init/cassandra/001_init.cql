-- Create keyspaces
CREATE KEYSPACE IF NOT EXISTS shop WITH replication = {'class':'SimpleStrategy','replication_factor':1};
CREATE KEYSPACE IF NOT EXISTS order_keyspace WITH replication = {'class':'SimpleStrategy','replication_factor':1};
CREATE KEYSPACE IF NOT EXISTS inventory_keyspace WITH replication = {'class':'SimpleStrategy','replication_factor':1};

-- Order tables
USE order_keyspace;
CREATE TABLE IF NOT EXISTS orders (
  id text PRIMARY KEY,
  user_id text,
  status text,
  total_amount decimal,
  shipping_address text,
  created_at timestamp,
  updated_at timestamp
);
CREATE TABLE IF NOT EXISTS order_items (
  order_id text,
  sku text,
  qty int,
  unit_price decimal,
  PRIMARY KEY(order_id, sku)
);

-- Inventory tables
USE inventory_keyspace;
CREATE TABLE IF NOT EXISTS inventory_by_sku (
  sku text PRIMARY KEY,
  available int,
  reserved int,
  updated_at timestamp
);
CREATE TABLE IF NOT EXISTS inventory_events (
  sku text,
  timeuuid timeuuid,
  type text,
  delta int,
  order_id text,
  created_at timestamp,
  PRIMARY KEY (sku, timeuuid)
) WITH CLUSTERING ORDER BY (timeuuid DESC);