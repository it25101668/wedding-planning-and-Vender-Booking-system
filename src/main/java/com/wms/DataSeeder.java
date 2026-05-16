package com.wms;

import com.wms.entity.User;
import com.wms.repository.UserRepository;
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
                if (!"SUPER_ADMIN".equals(existing.getRole())) {
                    existing.setRole("SUPER_ADMIN");
                    existing.setFullName("Super Admin");
                    userRepository.save(existing);
                    System.out.println("✅ Existing 'admin' user promoted to SUPER_ADMIN.");
                } else {
                    System.out.println("ℹ️ Super Admin already exists, skipping.");
                }
            });
        }
    }
}
