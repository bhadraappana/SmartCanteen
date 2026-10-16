package com.canteen.controller;

import com.canteen.model.Admin;
import com.canteen.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @PostMapping
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminRepository.save(admin);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Admin admin = adminRepository.findByEmail(email);
        if (admin != null && admin.getPassword().equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", admin.getId());
            response.put("name", admin.getName());
            response.put("email", admin.getEmail());
            response.put("canteen", admin.getCanteen());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            adminRepository.deleteById(id);
        } catch (NumberFormatException e) {
            Admin admin = adminRepository.findByEmail(identifier);
            if (admin != null) {
                adminRepository.deleteById(admin.getId());
            }
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody Admin adminDetails) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id " + id));
        admin.setName(adminDetails.getName());
        admin.setEmail(adminDetails.getEmail());
        admin.setPassword(adminDetails.getPassword());
        admin.setCanteenName(adminDetails.getCanteenName());
        admin.setPhone(adminDetails.getPhone());
        Admin updated = adminRepository.save(admin);
        return ResponseEntity.ok(updated);
    }
}
