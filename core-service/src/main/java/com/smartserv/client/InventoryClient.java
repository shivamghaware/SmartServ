package com.smartserv.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartserv.dto.inventory.InventoryResponseDto;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {

    @GetMapping("/{id}")
    InventoryResponseDto getItemById(@PathVariable("id") Long id);

    @PutMapping("/{id}/deduct-stock")
    ResponseEntity<Void> deductStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @PutMapping("/{id}/add-stock")
    ResponseEntity<Void> addStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}
