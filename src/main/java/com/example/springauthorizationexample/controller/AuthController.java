package com.example.springauthorizationexample.controller;

import com.example.springauthorizationexample.dto.LoginRequest;
import com.example.springauthorizationexample.dto.LoginResponse;
import com.example.springauthorizationexample.dto.RegisterUserRequest;
import com.example.springauthorizationexample.model.User;
import com.example.springauthorizationexample.repository.RoleRepository;
import com.example.springauthorizationexample.repository.UserRepository;
import com.example.springauthorizationexample.security.JwtUtil;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthController(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody RegisterUserRequest request) {
    var userRole = roleRepository.findByName("ROLE_USER");

    if (userRole.isEmpty()) {
      return ResponseEntity.badRequest().body("Default role not found.");
    }

    var user = new User();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRoles(Collections.singleton(userRole.get()));

    userRepository.save(user);
    return ResponseEntity.ok("User registered!");
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    return userRepository
        .findByUsername(request.username())
        .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
        .map(
            user -> ResponseEntity.ok(new LoginResponse(jwtUtil.generateToken(user.getUsername()))))
        .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout() {
    return ResponseEntity.ok("You have been logged out");
  }

  @PutMapping("/admin/change-role/{username}")
  public ResponseEntity<String> changeUserRole(
      @PathVariable String username, @RequestParam String newRole) {
    var user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    var role =
        roleRepository
            .findByName(newRole)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

    user.setRoles(Collections.singleton(role));
    userRepository.save(user);
    return ResponseEntity.ok("User role updated to %s".formatted(newRole));
  }
}
