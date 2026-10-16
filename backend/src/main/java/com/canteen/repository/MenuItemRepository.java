package com.canteen.repository;

import com.canteen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCanteen_Id(Long canteenId);
    boolean existsByNameAndCanteen_Id(String name, Long canteenId);
}
