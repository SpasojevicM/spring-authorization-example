package com.example.springauthorizationexample.service;

import com.example.springauthorizationexample.dto.LoginRequest;
import com.example.springauthorizationexample.dto.LoginResponse;
import com.example.springauthorizationexample.dto.RegisterUserRequest;

public interface AuthService {

  String registerUser(RegisterUserRequest request);

  LoginResponse loginUser(LoginRequest request);

  String changeUserRole(String username, String newRole);
}
