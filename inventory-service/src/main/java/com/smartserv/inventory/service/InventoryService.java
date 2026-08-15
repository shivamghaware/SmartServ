package com.smartserv.inventory.service;

import java.util.List;

import com.smartserv.inventory.dto.CreateInventoryDto;
import com.smartserv.inventory.dto.InventoryResponseDto;
import com.smartserv.inventory.dto.UpdateInventoryDto;

public interface InventoryService {

    InventoryResponseDto createItem(CreateInventoryDto dto);

    List<InventoryResponseDto> getAllItems();

    InventoryResponseDto getItemById(Long itemId);

    InventoryResponseDto getItemBySkuCode(String skuCode);

    InventoryResponseDto updateItem(Long itemId, UpdateInventoryDto dto);

    void deleteItem(Long itemId);

    List<InventoryResponseDto> getAvailableItems();

    List<InventoryResponseDto> getLowStockItems();

    List<InventoryResponseDto> getOutOfStockItems();

    List<InventoryResponseDto> searchItems(String keyword);

    void deductStock(Long itemId, Integer quantity);

    void addStock(Long itemId, Integer quantity);
}
