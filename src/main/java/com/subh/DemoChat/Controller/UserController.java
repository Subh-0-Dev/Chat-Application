package com.subh.DemoChat.Controller;


import com.subh.DemoChat.DTOs.AuthRequest;
import com.subh.DemoChat.DTOs.profileResponseDto;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.UserRepository;
import com.subh.DemoChat.Service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsService userDetailsService;



    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
    @GetMapping("/index")
    public String index() {
        return "index";
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            return ResponseEntity.badRequest().body("Invalid email address");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }


    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            String token = jwtUtil.generateToken(request.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
    @GetMapping("/api/user/me")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            return ResponseEntity.ok(new profileResponseDto(user.getId(), user.getUsername(), user.getEmail()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    @GetMapping("/api/users")
    public ResponseEntity<List<UserEntity>> getUsers() {
        try {
            List<UserEntity> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/api/users/search")
    public ResponseEntity<?> searchUsersByEmail(@RequestParam String email) {
        try {
            Optional<UserEntity> users = userRepository.findByEmail(email);
            if (users.isEmpty()) {
                users = userRepository.findByUsername(email);
                if(users.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found.");
                }

            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching users.");
        }
    }
}






