package com.example.springauthorizationexample.controller;

import com.example.springauthorizationexample.dto.LoginRequest;
import com.example.springauthorizationexample.dto.LoginResponse;
import com.example.springauthorizationexample.dto.RegisterUserRequest;
import com.example.springauthorizationexample.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  public static final String USER_REGISTERED_MESSAGE = "User registered!";
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody RegisterUserRequest request) {
    var response = authService.registerUser(request);
    return response.equals(USER_REGISTERED_MESSAGE)
        ? ResponseEntity.ok(response)
        : ResponseEntity.badRequest().body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    try {
      return ResponseEntity.ok(authService.loginUser(request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PutMapping("/admin/change-role/{username}")
  public ResponseEntity<String> changeUserRole(
      @PathVariable String username, @RequestParam String newRole) {
    return ResponseEntity.ok(authService.changeUserRole(username, newRole));
  }
}
