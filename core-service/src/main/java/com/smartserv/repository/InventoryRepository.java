package com.smartserv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartserv.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCode(String skuCode);

    Inventory findBySkuCode(String skuCode);

    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.stockQuantity > 0")
    List<Inventory> findAvailableItems();

    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.stockQuantity < 10")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND i.stockQuantity = 0")
    List<Inventory> findOutOfStockItems();


    @Query("SELECT i FROM Inventory i WHERE i.deleted = false AND " +
            "(LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(i.skuCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Inventory> searchByName(String keyword);
}

