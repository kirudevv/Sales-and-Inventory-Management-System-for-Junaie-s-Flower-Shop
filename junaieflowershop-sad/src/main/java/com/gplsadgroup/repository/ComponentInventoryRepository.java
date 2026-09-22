package com.gplsadgroup.repository;

import com.gplsadgroup.model.ComponentInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentInventoryRepository extends JpaRepository<ComponentInventory, Long> {
}