#!/usr/bin/env bash
set -euo pipefail
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic order.created --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic order.inventory_reserved --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic order.cancelled --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic payment.succeeded --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic payment.failed --replication-factor 1 --partitions 3
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic payment.refunded --replication-factor 1 --partitions 3
echo "All Kafka topics created successfully"