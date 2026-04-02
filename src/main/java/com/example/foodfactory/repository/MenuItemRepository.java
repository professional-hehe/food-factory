package com.example.foodfactory.repository;

import com.example.foodfactory.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
    List<MenuItem> findByRestaurantId(Long restaurantId);

    @Query("SELECT m FROM MenuItem m JOIN m.item i JOIN m.restaurant r " +
           "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND m.isAvailable = true AND r.isActive = true")
    List<MenuItem> searchByItemNameAvailable(@Param("name") String name);
}
