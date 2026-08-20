package com.smartserv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartserv.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCode(String skuCode);

    Inventory findBySkuCode(String skuCode);

    List<Inventory> findByDeletedFalseAndStockQuantityGreaterThan(Integer stockQuantity);

    List<Inventory> findByDeletedFalseAndStockQuantityLessThan(Integer stockQuantity);

    List<Inventory> findByDeletedFalseAndStockQuantity(Integer stockQuantity);

    List<Inventory> findByDeletedFalseAndItemNameContainingIgnoreCaseOrDeletedFalseAndSkuCodeContainingIgnoreCase(String itemName, String skuCode);
}

