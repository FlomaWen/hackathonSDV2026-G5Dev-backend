package com.group.hackathon_G5Dev.domain.service;

import com.group.hackathon_G5Dev.config.JwtService;
import com.group.hackathon_G5Dev.domain.exception.EmailAlreadyExistsException;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User register(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("L'email " + email + " est déjà utilisé");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        return userRepository.findByEmail(email)
                .orElseThrow();
    }

    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }
}
