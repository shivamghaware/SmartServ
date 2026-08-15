package com.smartserv.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="job_card_item")
@AttributeOverride(name="id", column=@Column(name="item_id"))
@Getter
@Setter

public class JobCardItem extends BaseEntity{

    @Column(name="quantity", nullable=false)
    private int quantity;

    @Column(name="snapshot_price", nullable=false)
    private Double snapshotPrice;

    @Column(name= "snapshot_item_name", nullable=false)
    private String snapshotItemName;

    @Column(name="total_price", nullable=false)
    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="job_card_id", nullable=false)
    private JobCard jobCard;

    @Column(name="product_id", nullable=false)
    private Long inventoryItemId;

}

