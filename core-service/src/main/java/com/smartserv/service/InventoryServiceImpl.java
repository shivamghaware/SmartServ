package com.smartserv.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.dto.inventory.CreateInventoryDto;
import com.smartserv.dto.inventory.InventoryResponseDto;
import com.smartserv.dto.inventory.UpdateInventoryDto;
import com.smartserv.entity.Inventory;
import com.smartserv.exceptions.DuplicateSkuException;
import com.smartserv.exceptions.InvalidOperationException;
import com.smartserv.exceptions.ResourceNotFoundException;
import com.smartserv.exceptions.StockConflictException;
import com.smartserv.repository.InventoryRepository;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository inventoryRepo;
	private static final int LOW_STOCK_THRESHOLD = 10;

	@Override
	public InventoryResponseDto createItem(CreateInventoryDto dto) {
		log.info("creating inventory item {}", dto.getItemName());

		if (inventoryRepo.existsBySkuCode(dto.getSkuCode())) {
			throw new DuplicateSkuException("item with sku code :" + dto.getSkuCode() + " already exists.");
		}

		Inventory item = new Inventory();
		item.setItemName(dto.getItemName());
		item.setSkuCode(dto.getSkuCode());
		item.setCurrentPrice(dto.getCurrentPrice());
		item.setStockQuantity(dto.getStockQuantity());
		item.setDeleted(false);

		Inventory saved = inventoryRepo.save(item);

		log.info("item created with id: {}", saved.getId());

		return mapToResponseDto(saved);
	}

	@Override
	public List<InventoryResponseDto> getAllItems() {
		List<Inventory> items = inventoryRepo.findAll();
		return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}

	@Override
	public InventoryResponseDto getItemById(Long itemId) {
		Inventory item = inventoryRepo.findById(itemId)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found"));

		return mapToResponseDto(item);
	}

	@Override
	public InventoryResponseDto getItemBySkuCode(String skuCode) {
		Inventory item = inventoryRepo.findBySkuCode(skuCode);

		return mapToResponseDto(item);
	}

	@Override
	public InventoryResponseDto updateItem(Long itemId, UpdateInventoryDto dto) {
		Inventory item = inventoryRepo.findById(itemId)
				.orElseThrow(() -> new ResourceNotFoundException("Item not found."));

		log.info("updating item with id: {} ", itemId);
		if (item.isDeleted()) {
			throw new InvalidOperationException("Deleted item cannot be updated.");
		}

		if (dto.getItemName() != null) {
			item.setItemName(dto.getItemName());
		}

		if (dto.getStockQuantity() != null) {
			item.setStockQuantity(dto.getStockQuantity());
		}

		if (dto.getCurrentPrice() != null) {
			item.setCurrentPrice(dto.getCurrentPrice());
		}

		try {
			Inventory updated = inventoryRepo.save(item);
			log.info("updated item {} ", updated.getId());
			return mapToResponseDto(updated);
		} catch (OptimisticLockException e) {
			throw new StockConflictException("Item was modified by another user. Please refresh and try again.");
		}
	}

	@Override
	public void deleteItem(Long itemId) {
		Inventory item = inventoryRepo.findById(itemId)
				.orElseThrow(() -> new ResourceNotFoundException("item not found."));
		log.info("deleting item {} ", itemId);

		item.setDeleted(true);

		inventoryRepo.save(item);

		log.info("item {} deleted", itemId);

	}

	@Override
	public List<InventoryResponseDto> getAvailableItems() {
		List<Inventory> inventories = inventoryRepo.findAvailableItems();
		return inventories.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}

	@Override
	public List<InventoryResponseDto> getLowStockItems() {
		List<Inventory> items = inventoryRepo.findLowStockItems();

		return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}

	@Override
	public List<InventoryResponseDto> getOutOfStockItems() {
		List<Inventory> items = inventoryRepo.findOutOfStockItems();

		return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}
	
	@Override
	public List<InventoryResponseDto> searchItems(String keyword) {
		List<Inventory> items = inventoryRepo.searchByName(keyword);
		return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
	}

	// ----------------Helper Methods----------------

	private InventoryResponseDto mapToResponseDto(Inventory inventory) {
		return InventoryResponseDto.builder().id(inventory.getId()).itemName(inventory.getItemName())
				.skuCode(inventory.getSkuCode()).currentPrice(inventory.getCurrentPrice())
				.stockQuantity(inventory.getStockQuantity()).deleted(inventory.isDeleted())
				.version(inventory.getVersion())
				.lowStock(inventory.getStockQuantity() > 0 && inventory.getStockQuantity() <= LOW_STOCK_THRESHOLD)
				.outOfStock(inventory.getStockQuantity() == 0).createdAt(inventory.getCreatedOn())
				.updatedAt(inventory.getLastUpdated()).build();
	}

	

}
