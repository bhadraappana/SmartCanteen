package com.canteen.repository;

import com.canteen.model.Order;
import com.canteen.model.Canteen;
import com.canteen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByRestaurantName(String restaurantName);
    List<Order> findByUser(User user);
    List<Order> findByCanteen(Canteen canteen);
}
