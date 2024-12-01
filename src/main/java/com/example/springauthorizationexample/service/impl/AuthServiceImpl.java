package com.example.springauthorizationexample.service.impl;

import com.example.springauthorizationexample.dto.LoginRequest;
import com.example.springauthorizationexample.dto.LoginResponse;
import com.example.springauthorizationexample.dto.RegisterUserRequest;
import com.example.springauthorizationexample.model.Role;
import com.example.springauthorizationexample.model.User;
import com.example.springauthorizationexample.repository.RoleRepository;
import com.example.springauthorizationexample.repository.UserRepository;
import com.example.springauthorizationexample.security.JwtUtil;
import com.example.springauthorizationexample.service.AuthService;
import java.util.Collections;
import java.util.HashSet;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

  public static final String USER_REGISTERED_MESSAGE = "User registered!";
  public static final String USERNAME_PASSWORD_INCORRECT = "Username or password incorrect!";
  public static final String USER_ROLE_UPDATED = "User role updated to %s";
  public static final String USER_NOT_FOUND = "User not found";
  public static final String ROLE_NOT_FOUND = "Role not found";
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  @Override
  public String registerUser(RegisterUserRequest request) {
    var userRole = roleRepository.findByName("ROLE_USER");

    if (userRole.isEmpty()) {
      return "Default role not found.";
    }

    var user = new User();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRoles(Collections.singleton(userRole.get()));

    userRepository.save(user);
    return USER_REGISTERED_MESSAGE;
  }

  @Override
  public LoginResponse loginUser(LoginRequest request) {
    return userRepository
        .findByUsername(request.username())
        .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
        .map(user -> new LoginResponse(jwtUtil.generateToken(user.getUsername())))
        .orElseThrow(() -> new IllegalArgumentException(USERNAME_PASSWORD_INCORRECT));
  }

  @Override
  public String changeUserRole(String username, String newRole) {
    var user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND));
    var role =
        roleRepository
            .findByName(newRole)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, ROLE_NOT_FOUND));

    var roles = new HashSet<Role>();
    roles.add(role);
    user.setRoles(roles);
    userRepository.save(user);

    return USER_ROLE_UPDATED.formatted(newRole);
  }
}
