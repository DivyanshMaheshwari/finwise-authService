package com.finwise.authservice.service;

import com.finwise.authservice.dto.AuthResponse;
import com.finwise.authservice.dto.LoginRequest;
import com.finwise.authservice.dto.RegisterRequest;
import com.finwise.authservice.entity.User;
import com.finwise.authservice.exception.InvalidCredentialsException;
import com.finwise.authservice.exception.UserAlreadyExistsException;
import com.finwise.authservice.exception.UserNotFoundException;
import com.finwise.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }

        String userId = generateUniqueUserId(request.getFirstName(), request.getLastName());

        if (userRepository.findByUserId(userId).isPresent()) {
            throw new UserAlreadyExistsException("User with generated userId already exists.");
        }

        User user = User.builder()
                .userId(userId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        userRepository.save(user);

        return "User registered successfully with ID: " + userId;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmailOrUserId())
                .or(() -> userRepository.findByUserId(request.getEmailOrUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found with provided email or userId."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password.");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
    private String generateUniqueUserId(String firstName, String lastName) {
        String prefix = (firstName.substring(0, 1) + lastName.substring(0, 1)).toLowerCase();
        String suffix;
        String userId;

        do {
            suffix = String.format("%06d", new Random().nextInt(999999));
            userId = prefix + suffix;
        } while (userRepository.existsByUserId(userId));

        return userId;
    }
}