package com.icc.inventoryservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.datastax.oss.driver.api.core.uuid.Uuids;

@Table("inventory_events")
public class InventoryEvent {

    @PrimaryKeyClass
    public static class InventoryEventKey {
        @PrimaryKeyColumn(name = "sku", type = PrimaryKeyType.PARTITIONED)
        private String sku;

        @PrimaryKeyColumn(name = "timeuuid", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private UUID timeuuid;

        public InventoryEventKey() {}

        public InventoryEventKey(String sku, UUID timeuuid) {
            this.sku = sku;
            this.timeuuid = timeuuid;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public UUID getTimeuuid() {
            return timeuuid;
        }

        public void setTimeuuid(UUID timeuuid) {
            this.timeuuid = timeuuid;
        }
    }

    @PrimaryKey
    private InventoryEventKey key;

    @Column("delta")
    private Integer delta;

    @Column("order_id")
    private String orderId;

    @Column("type")
    private String type;

    @Column("created_at")
    private LocalDateTime createdAt;

    public InventoryEvent() {}

    public InventoryEvent(String sku, Integer delta, String orderId, String type) {
        this.key = new InventoryEventKey(sku, Uuids.timeBased());
        this.delta = delta;
        this.orderId = orderId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public InventoryEventKey getKey() {
        return key;
    }

    public void setKey(InventoryEventKey key) {
        this.key = key;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}