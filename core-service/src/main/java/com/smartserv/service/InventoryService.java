package com.smartserv.service;

import java.util.List;

import com.smartserv.dto.inventory.CreateInventoryDto;
import com.smartserv.dto.inventory.InventoryResponseDto;
import com.smartserv.dto.inventory.UpdateInventoryDto;

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

	
	
}
