package com.lostfound.lostfoundportal.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.lostfound.lostfoundportal.model.User;
import com.lostfound.lostfoundportal.repository.UserRepository;

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

        user.setRole("USER");

        userRepository.save(user);
    }
    public User findByEmail(String email) {

    return userRepository
            .findByEmail(email)
            .orElse(null);
}
}