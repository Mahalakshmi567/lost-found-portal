package com.lostfound.lostfoundportal.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.lostfound.lostfoundportal.model.Role;
import com.lostfound.lostfoundportal.model.User;
import com.lostfound.lostfoundportal.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {

        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        // Public registration always creates a plain USER account.
        // Nobody can self-register as ADMIN or MODERATOR - those roles
        // are only granted afterwards, from the admin panel.
        user.setRole(Role.USER);

        userRepository.save(user);
    }

    public User findByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(Long userId, Role role) {

        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setRole(role);
            userRepository.save(user);
        }

        return user;
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}