package com.gplsadgroup.service;

import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public void initDefaultUser() {
        if (userRepository.count() == 0) {
            User owner = new User();
            owner.setUsername("owner");
            owner.setPasswordHash("owner123");
            owner.setFullName("Store Owner");
            owner.setRole("OWNER");
            userRepository.save(owner);
            System.out.println(">>> Initialized default Owner account: owner / owner123");
        }
    }

    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPasswordHash().equals(password)) {
            return userOpt;
        }
        return Optional.empty();
    }

    public User createNewUser(String fullName, String username, String password, String role) {
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setPasswordHash(password);
        newUser.setRole(role.toUpperCase());
        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public void updateUser(Long userId, String fullName, String username, String newPassword, String role) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    user.setFullName(fullName);
    user.setUsername(username);
    user.setRole(role);

    // Only update the password if a new one was provided
    if (newPassword != null && !newPassword.trim().isEmpty()) {
        // If you are hashing passwords (e.g. BCrypt), hash it here:
        // user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordHash(newPassword);
    }

    userRepository.save(user);
}
}