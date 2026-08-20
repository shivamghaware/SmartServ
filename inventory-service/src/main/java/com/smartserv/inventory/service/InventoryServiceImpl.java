package com.smartserv.inventory.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.inventory.dto.CreateInventoryDto;
import com.smartserv.inventory.dto.InventoryResponseDto;
import com.smartserv.inventory.dto.UpdateInventoryDto;
import com.smartserv.inventory.entity.Inventory;
import com.smartserv.inventory.exceptions.DuplicateSkuException;
import com.smartserv.inventory.exceptions.InsufficientStockException;
import com.smartserv.inventory.exceptions.InvalidOperationException;
import com.smartserv.inventory.exceptions.ResourceNotFoundException;
import com.smartserv.inventory.exceptions.StockConflictException;
import com.smartserv.inventory.repository.InventoryRepository;

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
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getAllItems() {
        List<Inventory> items = inventoryRepo.findAll();
        return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getItemById(Long itemId) {
        Inventory item = inventoryRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        return mapToResponseDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getItemBySkuCode(String skuCode) {
        Inventory item = inventoryRepo.findBySkuCode(skuCode);
        if (item == null) {
            throw new ResourceNotFoundException("Item not found with SKU: " + skuCode);
        }
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
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getAvailableItems() {
        List<Inventory> inventories = inventoryRepo.findByDeletedFalseAndStockQuantityGreaterThan(0);
        return inventories.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getLowStockItems() {
        List<Inventory> items = inventoryRepo.findByDeletedFalseAndStockQuantityLessThan(LOW_STOCK_THRESHOLD);
        return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getOutOfStockItems() {
        List<Inventory> items = inventoryRepo.findByDeletedFalseAndStockQuantity(0);
        return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> searchItems(String keyword) {
        List<Inventory> items = inventoryRepo.findByDeletedFalseAndItemNameContainingIgnoreCaseOrDeletedFalseAndSkuCodeContainingIgnoreCase(keyword, keyword);
        return items.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deductStock(Long itemId, Integer quantity) {
        Inventory item = inventoryRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found."));

        if (item.isDeleted()) {
            throw new InvalidOperationException("Cannot modify stock of a deleted item.");
        }

        if (item.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Available: " 
                    + item.getStockQuantity() + ", requested: " + quantity);
        }

        try {
            item.setStockQuantity(item.getStockQuantity() - quantity);
            inventoryRepo.save(item);
            log.info("Deducted {} stock units from item ID {}. New stock: {}", quantity, itemId, item.getStockQuantity());
        } catch (OptimisticLockException e) {
            throw new StockConflictException("Stock conflict. The item was modified by another thread. Please retry.");
        }
    }

    @Override
    @Transactional
    public void addStock(Long itemId, Integer quantity) {
        Inventory item = inventoryRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found."));

        if (item.isDeleted()) {
            throw new InvalidOperationException("Cannot add stock to a deleted item.");
        }

        try {
            item.setStockQuantity(item.getStockQuantity() + quantity);
            inventoryRepo.save(item);
            log.info("Added {} stock units to item ID {}. New stock: {}", quantity, itemId, item.getStockQuantity());
        } catch (OptimisticLockException e) {
            throw new StockConflictException("Stock conflict. The item was modified by another thread. Please retry.");
        }
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
