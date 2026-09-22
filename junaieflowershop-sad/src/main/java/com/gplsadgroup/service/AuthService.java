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
}