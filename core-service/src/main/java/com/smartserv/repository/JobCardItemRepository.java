package com.smartserv.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartserv.entity.JobCardItem;

public interface JobCardItemRepository extends JpaRepository<JobCardItem, Long> {
    Optional<JobCardItem> findByIdAndJobCardId(Long itemId, Long jobCardId);
}

