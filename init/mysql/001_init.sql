CREATE DATABASE IF NOT EXISTS account CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS payment CHARACTER SET utf8mb4;
USE account;
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  username VARCHAR(255),
  password_hash VARCHAR(255),
  shipping_address TEXT,
  billing_address TEXT,
  payment_method VARCHAR(255)
);
USE payment;
CREATE TABLE IF NOT EXISTS payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id VARCHAR(64) NOT NULL UNIQUE,
  amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  provider_ref VARCHAR(128),
  payment_method VARCHAR(255),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  UNIQUE INDEX ux_pay_idem (order_id)
);
-- Idempotency: Only one payment per order_id