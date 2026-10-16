package com.canteen.controller;

import com.canteen.model.Canteen;
import com.canteen.model.Feedback;
import com.canteen.model.MenuItem;
import com.canteen.repository.CanteenRepository;
import com.canteen.repository.FeedbackRepository;
import com.canteen.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/canteens/{canteenId}/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CanteenRepository canteenRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Feedback> getAllFeedback(@PathVariable Long canteenId) {
        Canteen canteen = canteenRepository.findById(canteenId).orElse(null);
        if (canteen == null) {
            return new ArrayList<>();
        }
        List<Feedback> feedback = feedbackRepository.findByCanteen(canteen);
        feedback.forEach(f -> {
            if (f.getMenuItem() != null) f.getMenuItem().getName();
            if (f.getCanteen() != null) f.getCanteen().getName();
        });
        return feedback;
    }

    @GetMapping("/item/{itemId}")
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackForItem(@PathVariable Long canteenId, @PathVariable Long itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId).orElse(null);
        if (menuItem == null) {
            return new ArrayList<>();
        }
        List<Feedback> feedback = feedbackRepository.findByMenuItem(menuItem);
        feedback.forEach(f -> {
            if (f.getMenuItem() != null) f.getMenuItem().getName();
        });
        return feedback;
    }

    @GetMapping("/item/{itemId}/average")
    @Transactional(readOnly = true)
    public Map<String, Object> getAverageRating(@PathVariable Long canteenId, @PathVariable Long itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId).orElse(null);
        Map<String, Object> result = new HashMap<>();
        if (menuItem == null) {
            result.put("average", 0);
            result.put("count", 0);
            return result;
        }
        List<Feedback> feedback = feedbackRepository.findByMenuItem(menuItem);
        double average = feedback.stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        result.put("average", Math.round(average * 10.0) / 10.0);
        result.put("count", feedback.size());
        return result;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> addFeedback(@PathVariable Long canteenId, @RequestBody Map<String, Object> payload) {
        Canteen canteen = canteenRepository.findById(canteenId).orElse(null);
        if (canteen == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Canteen not found with id " + canteenId));
        }

        Feedback feedback = new Feedback();
        feedback.setCanteen(canteen);

        String username = (String) payload.get("username");
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous";
        }
        feedback.setUsername(username);

        Object ratingObj = payload.get("rating");
        if (ratingObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rating is required"));
        }
        int rating = Integer.parseInt(ratingObj.toString());
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rating must be between 1 and 5"));
        }
        feedback.setRating(rating);
        feedback.setComment((String) payload.get("comment"));

        if (payload.get("itemId") != null) {
            Long itemId = Long.valueOf(payload.get("itemId").toString());
            MenuItem menuItem = menuItemRepository.findById(itemId).orElse(null);
            if (menuItem != null) {
                feedback.setMenuItem(menuItem);
            }
        }

        Feedback saved = feedbackRepository.save(feedback);
        if (saved.getMenuItem() != null) saved.getMenuItem().getName();
        if (saved.getCanteen() != null) saved.getCanteen().getName();
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long canteenId, @PathVariable Long id) {
        feedbackRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
