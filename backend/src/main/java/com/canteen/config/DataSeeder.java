package com.canteen.config;

import com.canteen.model.Admin;
import com.canteen.model.Canteen;
import com.canteen.model.MenuItem;
import com.canteen.model.User;
import com.canteen.repository.AdminRepository;
import com.canteen.repository.CanteenRepository;
import com.canteen.repository.MenuItemRepository;
import com.canteen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataSeeder {
    private static final String DEFAULT_PASSWORD = "$2a$10$8JJ8v0ZlZqG6QZ6QZ6QZ6Oe5J6J6J6J6J6J6J6J6J6J6J6J6J6J6";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CanteenRepository canteenRepository;

    @Bean
    @Transactional
    CommandLineRunner seedData() {
        return args -> {
            seedUsers();
            seedAdmins();
            seedCanteen("Ball Canteen 1", "Main Campus", new String[][]{
                    {"Paneer Masala Meal", "180", "Veg", "\uD83C\uDDFA", "LUNCH"},
                    {"Chicken Biryani", "220", "Non-Veg", "\uD83C\uDDF7", "LUNCH"},
                    {"Grilled Fish", "250", "Non-Veg", "\uD83D\uDC4F", "LUNCH"},
                    {"Greek Salad", "120", "Veg", "\uD83D\uDC57", "LUNCH"},
                    {"Chocolate Cake", "450", "Cakes", "\uD83D\uDC78", "EVENING"},
                    {"Vanilla Pastry", "80", "Cakes", "\uD83D\uDC78", "EVENING"},
                    {"Chocolate Cone", "60", "Icecreams", "\uD83D\uDC76", "EVENING"},
                    {"Mango Sorbet", "90", "Icecreams", "\uD83D\uDC77", "EVENING"},
                    {"Fresh Watermelon Juice", "60", "Drinks", "\uD83D\uDCA0", "BOTH"},
                    {"Coca Cola 500ml", "45", "Drinks", "\uD83E\uDD44", "BOTH"},
                    {"Dairy Milk Silk", "150", "Choclates", "\uD83D\uDC6B", "EVENING"},
                    {"Ferrero Rocher", "300", "Choclates", "\uD83D\uDC6C", "EVENING"}
            });
            seedCanteen("Main Canteen", "Building A, Floor 1", new String[][]{
                    {"Burger", "5.99", "Main Course", "🍔", "LUNCH"},
                    {"Pizza", "8.99", "Main Course", "🍕", "BOTH"},
                    {"Salad", "4.99", "Side", "🥗", "LUNCH"},
                    {"Coke", "1.99", "Beverage", "🥤", "BOTH"},
                    {"Ice Cream", "2.99", "Dessert", "🍦", "EVENING"}
            });
            seedCanteen("East Canteen", "Building B, Floor 2", new String[][]{
                    {"Sandwich", "4.99", "Main Course", "🥪", "LUNCH"},
                    {"Pasta", "7.99", "Main Course", "🍝", "LUNCH"},
                    {"Fries", "2.99", "Side", "🍟", "EVENING"},
                    {"Juice", "2.49", "Beverage", "🥤", "BOTH"},
                    {"Cake", "3.99", "Dessert", "🍰", "EVENING"}
            });
        };
    }

    private void seedUsers() {
        seedUser("john_doe", DEFAULT_PASSWORD, "John Doe", "john@example.com", "USER", "1234567890");
        seedUser("testuser2", DEFAULT_PASSWORD, "Test User Two", "testuser2@example.com", "USER", "9998887776");
    }

    private void seedUser(String username, String password, String fullName, String email, String role, String phone) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setPhone(phone);
        userRepository.save(user);
    }

    private void seedAdmins() {
        seedAdmin("admin1@example.com", DEFAULT_PASSWORD, "Admin One", "Main Canteen", "1111111111");
        seedAdmin("admin2@example.com", DEFAULT_PASSWORD, "Admin Two", "East Canteen", "2222222222");
    }

    private void seedAdmin(String email, String password, String name, String canteen, String phone) {
        if (adminRepository.existsByEmail(email)) {
            return;
        }

        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(password);
        admin.setName(name);
        admin.setCanteen(canteen);
        admin.setPhone(phone);
        adminRepository.save(admin);
    }

    private void seedCanteen(String name, String location, String[][] menuItemsData) {
        Canteen canteen = findOrCreateCanteen(name, location);
        for (String[] item : menuItemsData) {
            if (!menuItemRepository.existsByNameAndCanteen_Id(item[0], canteen.getId())) {
                MenuItem menuItem = new MenuItem();
                menuItem.setName(item[0]);
                menuItem.setPrice(Double.parseDouble(item[1]));
                menuItem.setCategory(item[2]);
                menuItem.setEmoji(item[3]);
                menuItem.setStockQuantity(50);
                menuItem.setAvailable(true);
                menuItem.setCanteen(canteen);
                // Set slot if provided (5th element), otherwise default to BOTH
                if (item.length >= 5 && item[4] != null && !item[4].isEmpty()) {
                    menuItem.setSlot(item[4]);
                } else {
                    menuItem.setSlot("BOTH");
                }
                menuItemRepository.save(menuItem);
            }
        }
    }

    private Canteen findOrCreateCanteen(String name, String location) {
        Canteen existing = canteenRepository.findByName(name).stream()
                .sorted((first, second) -> Long.compare(first.getId(), second.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setLocation(location);
            existing.setActive(true);
            return canteenRepository.save(existing);
        }

    Canteen canteen = new Canteen();
        canteen.setName(name);
        canteen.setLocation(location);
        canteen.setActive(true);
        // Default session timings (12-hour AM/PM format)
        canteen.setLunchStart("09:00 AM");
        canteen.setLunchEnd("02:00 PM");
        canteen.setEveningStart("01:30 PM");
        canteen.setEveningEnd("08:00 PM");
        return canteenRepository.save(canteen);
    }
}
