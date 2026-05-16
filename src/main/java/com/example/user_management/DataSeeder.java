package com.example.user_management;

import com.example.user_management.entity.User;
import com.example.user_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            // Create fresh super admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setEmail("admin@wms.com");
            admin.setFullName("Super Admin");
            admin.setRole("SUPER_ADMIN");
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("✅ Super Admin created (username: admin, password: admin)");
        } else {
            // Existing admin — promote to SUPER_ADMIN if not already
            userRepository.findByUsername("admin").ifPresent(existing -> {
                if (!"SUPER_ADMIN".equals(existing.getRole()) || !"admin".equals(existing.getPassword())) {
                    existing.setRole("SUPER_ADMIN");
                    existing.setFullName("Super Admin");
                    existing.setPassword("admin");
                    userRepository.save(existing);
                    System.out.println("✅ Existing 'admin' user updated with default credentials.");
                } else {
                    System.out.println("ℹ️ Super Admin already exists with correct credentials, skipping.");
                }
            });
        }
    }
}
