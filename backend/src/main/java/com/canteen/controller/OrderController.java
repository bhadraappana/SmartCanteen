package com.canteen.controller;

import com.canteen.model.Order;
import com.canteen.model.OrderItem;
import com.canteen.model.MenuItem;
import com.canteen.model.User;
import com.canteen.repository.OrderRepository;
import com.canteen.repository.MenuItemRepository;
import com.canteen.repository.UserRepository;
import com.canteen.repository.CanteenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CanteenRepository canteenRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        orders.forEach(order -> {
            order.getItems().size(); // Force initialization of items
            order.getUser().getUsername(); // Force initialization of user
        });
        return orders;
    }

    @GetMapping("/user/{userId}")
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        List<Order> orders = orderRepository.findByUser(user);
        orders.forEach(order -> {
            order.getItems().size();
            order.getUser().getUsername();
        });
        return orders;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData) {
        Order order = new Order();

        // Handle user
        Map<String, Object> userMap = (Map<String, Object>) orderData.get("user");
        if (userMap == null || userMap.get("id") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User information is required"));
        }
        Long userId = Long.valueOf(userMap.get("id").toString());
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found with id " + userId));
        }
        order.setUser(user);

        // Handle items
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) orderData.get("items");
        if (itemsData == null || itemsData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Order must contain at least one item"));
        }
        for (Map<String, Object> itemData : itemsData) {
            OrderItem orderItem = new OrderItem();
            Map<String, Object> menuItemMap = (Map<String, Object>) itemData.get("menuItem");
            if (menuItemMap == null || menuItemMap.get("id") == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Menu item information is required for each item"));
            }
            Long menuItemId = Long.valueOf(menuItemMap.get("id").toString());
            MenuItem menuItem = menuItemRepository.findById(menuItemId).orElse(null);
            if (menuItem == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Menu item not found with id " + menuItemId));
            }
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
        }

        if (orderData.get("totalAmount") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Total amount is required"));
        }
        order.setTotalAmount(new java.math.BigDecimal(orderData.get("totalAmount").toString()));
        order.setPaymentMethod((String) orderData.get("paymentMethod"));
        order.setTransactionId((String) orderData.get("transactionId"));
        order.setRestaurantName((String) orderData.get("restaurantName"));
        order.setSession((String) orderData.get("session"));
        order.setUniqueId((String) orderData.get("uniqueId"));
        order.setOrderTime(LocalDateTime.now());
        // Set canteen based on restaurantName
        try {
            com.canteen.model.Canteen canteen = canteenRepository.findAll().stream()
                .filter(c -> c.getName().equals(orderData.get("restaurantName")))
                .findFirst().orElse(null);
            if (canteen != null) {
                order.setCanteen(canteen);
            }
        } catch (Exception e) {
            // Canteen not found, continue without setting
        }

        Order saved = orderRepository.save(order);
        saved.getUser().getUsername(); // Initialize user for JSON serialization
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status.toUpperCase());
            if ("COMPLETED".equals(status.toUpperCase())) {
                order.setCompletedTime(LocalDateTime.now());
            }
            Order saved = orderRepository.save(order);
            saved.getUser().getUsername(); // Initialize user for JSON serialization
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatusPut(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status.toUpperCase());
            if ("COMPLETED".equals(status.toUpperCase())) {
                order.setCompletedTime(LocalDateTime.now());
            }
            Order saved = orderRepository.save(order);
            saved.getUser().getUsername();
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        if (updates.containsKey("status")) {
            order.setStatus(updates.get("status").toString().toUpperCase());
            if ("COMPLETED".equals(order.getStatus())) {
                order.setCompletedTime(LocalDateTime.now());
            }
        }
        if (updates.containsKey("paymentStatus")) {
            order.setPaymentStatus(updates.get("paymentStatus").toString().toUpperCase());
        }
        Order saved = orderRepository.save(order);
        saved.getUser().getUsername();
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/payment")
    @Transactional
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setPaymentStatus(paymentStatus.toUpperCase());
            Order saved = orderRepository.save(order);
            saved.getUser().getUsername(); // Initialize user for JSON serialization
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }
}