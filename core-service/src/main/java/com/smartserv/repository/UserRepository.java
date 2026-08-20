package com.smartserv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartserv.entity.Role;
import com.smartserv.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByIsActiveTrue();

    Page<User> findByIsActiveTrue(Pageable pageable);

    List<User> findByUserRole(Role role);

    List<User> findByUserRoleAndIsActiveTrue(Role role);

    Page<User> findByUserRoleAndIsActiveTrue(Role role, Pageable pageable);

    List<User> findByManagerId(Long managerId);

    List<User> findByManagerIdAndIsActiveTrue(Long managerId);

}

