package com.smartserv.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="users")
@AttributeOverride(name="id", column=@Column(name="user_id"))
@Getter
@Setter
public class User extends BaseEntity{

    @Column(name="user_name", nullable=false)
    private String userName;

    @Column(unique=true, nullable=false)
    private String email;

    @Column(nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role userRole;

    private String mobile;

    private Double salary;

    @Column(name="is_active")
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id")
    private User manager;
}