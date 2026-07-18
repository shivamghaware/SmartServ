package com.smartserv.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="inventory")
@AttributeOverride(name="id", column = @Column(name="product_id"))
@Getter
@Setter
public class Inventory extends BaseEntity{

    @Column(name="item_name")
    private String itemName;

    @Column(name="sku_code", unique = true)
    private String skuCode;

    @Column(name="current_price", nullable=false)
    private Double currentPrice;

    @Column(name="stock_quantity")
    private Integer stockQuantity;

    @Column(name="is_deleted")
    private boolean deleted;

    @Version
    private Integer version;
}

