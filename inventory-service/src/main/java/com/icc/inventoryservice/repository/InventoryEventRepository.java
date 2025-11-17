package com.icc.inventoryservice.repository;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.icc.inventoryservice.model.InventoryEvent;

@Repository
public interface InventoryEventRepository extends CassandraRepository<InventoryEvent, InventoryEvent.InventoryEventKey> {
    
    @Query("SELECT * FROM inventory_events WHERE sku = ?0")
    List<InventoryEvent> findByKeySku(String sku);
}