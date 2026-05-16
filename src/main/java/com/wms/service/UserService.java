package com.wms.service;

import com.wms.entity.User;
import com.wms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User addUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update username only if provided and different
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            if (!existing.getUsername().equals(updatedUser.getUsername()) &&
                    userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new RuntimeException("Username already exists: " + updatedUser.getUsername());
            }
            existing.setUsername(updatedUser.getUsername());
        }

        // Update email only if provided and different
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            if (!existing.getEmail().equals(updatedUser.getEmail()) &&
                    userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new RuntimeException("Email already exists: " + updatedUser.getEmail());
            }
            existing.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getFullName() != null && !updatedUser.getFullName().isEmpty()) {
            existing.setFullName(updatedUser.getFullName());
        }

        if (updatedUser.getProfilePic() != null && !updatedUser.getProfilePic().isEmpty()) {
            existing.setProfilePic(updatedUser.getProfilePic());
        }

        if (updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty()) {
            existing.setPhone(updatedUser.getPhone());
        }

        if (updatedUser.getRole() != null && !updatedUser.getRole().isEmpty()) {
            existing.setRole(updatedUser.getRole());
        }

        // Update password only if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existing.setPassword(updatedUser.getPassword());
        }

        existing.setActive(updatedUser.isActive());

        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(keyword.trim());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getActiveUsers() {
        return userRepository.findByActive(true).size();
    }
}