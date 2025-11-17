package com.icc.inventoryservice.repository;

import java.util.Optional;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.icc.inventoryservice.model.Inventory;

@Repository
public interface InventoryRepository extends CassandraRepository<Inventory, String> {
    
    Optional<Inventory> findBySku(String sku);
}