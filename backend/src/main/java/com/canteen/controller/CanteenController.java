package com.canteen.controller;

import com.canteen.model.Canteen;
import com.canteen.repository.CanteenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/canteens")
public class CanteenController {

    @Autowired
    private CanteenRepository canteenRepository;

    @GetMapping
    public List<Canteen> getAllCanteens() {
        return canteenRepository.findAll().stream()
                .sorted(Comparator.comparingLong(Canteen::getId))
                .collect(Collectors.toMap(
                        Canteen::getName,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    @PostMapping
    public Canteen createCanteen(@RequestBody Canteen canteen) {
        if (canteen.getName() != null && !canteen.getName().trim().isEmpty()
                && (canteen.getLocation() == null || canteen.getLocation().trim().isEmpty())) {
            canteen.setLocation("Not specified");
        }
        if (!canteen.isActive()) {
            canteen.setActive(true);
        }
        return canteenRepository.save(canteen);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCanteen(@PathVariable Long id) {
        canteenRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Canteen> updateCanteen(@PathVariable Long id, @RequestBody Canteen canteenDetails) {
        Canteen canteen = canteenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canteen not found with id " + id));
        if (canteenDetails.getName() != null) canteen.setName(canteenDetails.getName());
        if (canteenDetails.getLocation() != null) canteen.setLocation(canteenDetails.getLocation());
        canteen.setActive(canteenDetails.isActive());
        if (canteenDetails.getLunchStart() != null) canteen.setLunchStart(canteenDetails.getLunchStart());
        if (canteenDetails.getLunchEnd() != null) canteen.setLunchEnd(canteenDetails.getLunchEnd());
        if (canteenDetails.getEveningStart() != null) canteen.setEveningStart(canteenDetails.getEveningStart());
        if (canteenDetails.getEveningEnd() != null) canteen.setEveningEnd(canteenDetails.getEveningEnd());
        Canteen updated = canteenRepository.save(canteen);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/")
    public String home() {
        return "Canteen API is running perfectly!";
    }
}