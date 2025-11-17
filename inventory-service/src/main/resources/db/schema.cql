-- Create keyspace if not exists
CREATE KEYSPACE IF NOT EXISTS inventory_keyspace 
WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE inventory_keyspace;

-- Create inventory_by_sku table
CREATE TABLE IF NOT EXISTS inventory_by_sku (
    sku text PRIMARY KEY,
    available int,
    reserved int,
    updated_at timestamp
);

-- Create inventory_events table
CREATE TABLE IF NOT EXISTS inventory_events (
    sku text,
    timeuuid timeuuid,
    delta int,
    order_id text,
    type text,
    created_at timestamp,
    PRIMARY KEY (sku, timeuuid)
) WITH CLUSTERING ORDER BY (timeuuid DESC);

-- Insert some sample data for testing
INSERT INTO inventory_by_sku (sku, available, reserved, updated_at) 
VALUES ('SAMPLE-001', 100, 0, toTimestamp(now()));

INSERT INTO inventory_by_sku (sku, available, reserved, updated_at) 
VALUES ('SAMPLE-002', 50, 10, toTimestamp(now()));

INSERT INTO inventory_by_sku (sku, available, reserved, updated_at) 
VALUES ('SAMPLE-003', 200, 5, toTimestamp(now()));