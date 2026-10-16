package com.canteen.controller;

import com.canteen.model.Canteen;
import com.canteen.model.MenuItem;
import com.canteen.repository.CanteenRepository;
import com.canteen.repository.MenuItemRepository;
import com.canteen.service.PexelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/canteens/{canteenId}/menu")
public class MenuItemController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CanteenRepository canteenRepository;

    @Autowired
    private PexelsService pexelsService;

    @GetMapping
    public List<MenuItem> getMenuItems(@PathVariable Long canteenId) {
        Canteen canteen = canteenRepository.findById(canteenId)
                .orElseThrow(() -> new ResourceNotFoundException("Canteen not found with id " + canteenId));
        if (!canteen.isActive()) {
            throw new ResourceNotFoundException("Canteen is not active with id " + canteenId);
        }
        return menuItemRepository.findByCanteen_Id(canteenId).stream()
                .sorted(Comparator.comparingLong(MenuItem::getId))
                .collect(Collectors.toMap(
                        MenuItem::getName,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    @PostMapping
    public MenuItem addMenuItem(@PathVariable Long canteenId, @RequestBody MenuItem menuItem) {
        Canteen canteen = canteenRepository.findById(canteenId)
                .orElseThrow(() -> new ResourceNotFoundException("Canteen not found with id " + canteenId));
        if (!canteen.isActive()) {
            throw new ResourceNotFoundException("Cannot add items to inactive canteen with id " + canteenId);
        }
        if (menuItem.getStockQuantity() == null) {
            menuItem.setStockQuantity(50);
        }
        if (menuItem.isAvailable() == null) {
            menuItem.setAvailable(menuItem.getStockQuantity() > 0);
        } else {
            menuItem.setAvailable(menuItem.isAvailable() && menuItem.getStockQuantity() > 0);
        }
        if (menuItem.getSlot() == null) {
            menuItem.setSlot("BOTH");
        }
        String imageUrl = pexelsService.fetchImageUrl(menuItem.getName());
        if (imageUrl != null && !imageUrl.isBlank()) {
            menuItem.setImageUrl(imageUrl);
        }
        menuItem.setCanteenId(canteenId);
        return menuItemRepository.save(menuItem);
    }

    @PutMapping("/{id}")
    public MenuItem updateMenuItem(@PathVariable Long canteenId, @PathVariable Long id, @RequestBody MenuItem menuItemDetails) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id " + id));

        menuItem.setName(menuItemDetails.getName());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setCategory(menuItemDetails.getCategory());
        menuItem.setEmoji(menuItemDetails.getEmoji());
        String imageUrl = pexelsService.fetchImageUrl(menuItemDetails.getName());
        if (imageUrl != null && !imageUrl.isBlank()) {
            menuItem.setImageUrl(imageUrl);
        } else if (menuItemDetails.getImageUrl() != null) {
            menuItem.setImageUrl(menuItemDetails.getImageUrl());
        }
        if (menuItemDetails.getStockQuantity() != null) {
            menuItem.setStockQuantity(menuItemDetails.getStockQuantity());
        }
        menuItem.setAvailable(menuItem.getStockQuantity() > 0);
        if (menuItemDetails.getSlot() != null) {
            menuItem.setSlot(menuItemDetails.getSlot());
        }

        // Ensure the canteenId is set to the one from the path (for security)
        menuItem.setCanteenId(canteenId);

        return menuItemRepository.save(menuItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long canteenId, @PathVariable Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id " + id));

        // Optional: check that the menuItem belongs to the canteenId
        if (!menuItem.getCanteenId().equals(canteenId)) {
            return ResponseEntity.badRequest().build();
        }

        menuItemRepository.delete(menuItem);
        return ResponseEntity.ok().build();
    }
}

// Simple exception class for resource not found
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}