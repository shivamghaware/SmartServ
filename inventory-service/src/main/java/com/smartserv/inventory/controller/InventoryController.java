package com.smartserv.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartserv.inventory.dto.CreateInventoryDto;
import com.smartserv.inventory.dto.UpdateInventoryDto;
import com.smartserv.inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody CreateInventoryDto dto) {
        return ResponseEntity.ok(inventoryService.createItem(dto));
    }

    @GetMapping
    public ResponseEntity<?> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItemById(id));
    }
    
    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<?> getItemBySkuCode(@PathVariable String skuCode){
        return ResponseEntity.ok(inventoryService.getItemBySkuCode(skuCode));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody UpdateInventoryDto dto){
        return ResponseEntity.ok(inventoryService.updateItem(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id){
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
    
    //------------filters and search--------------
    
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableItems(){
        return ResponseEntity.ok(inventoryService.getAvailableItems());
    }
    
    @GetMapping("/low_stock")
    public ResponseEntity<?> getLowStockItems(){
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }
    
    @GetMapping("/out_of_stock")
    public ResponseEntity<?> getOutOfStockItems(){
        return ResponseEntity.ok(inventoryService.getOutOfStockItems());
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchItem(@RequestParam String keyword){
        return ResponseEntity.ok(inventoryService.searchItems(keyword));
    }
    
    //------------stock adjustments for inter-service communication--------------

    @PutMapping("/{id}/deduct-stock")
    public ResponseEntity<Void> deductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        inventoryService.deductStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/add-stock")
    public ResponseEntity<Void> addStock(@PathVariable Long id, @RequestParam Integer quantity) {
        inventoryService.addStock(id, quantity);
        return ResponseEntity.ok().build();
    }
}
