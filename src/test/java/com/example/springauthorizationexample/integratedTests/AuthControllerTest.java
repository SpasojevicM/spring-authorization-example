package com.example.springauthorizationexample.integratedTests;

import static com.example.springauthorizationexample.controller.AuthController.USER_REGISTERED_MESSAGE;
import static com.example.springauthorizationexample.service.impl.AuthServiceImpl.ROLE_NOT_FOUND;
import static com.example.springauthorizationexample.service.impl.AuthServiceImpl.USER_NOT_FOUND;
import static com.example.springauthorizationexample.service.impl.AuthServiceImpl.USER_ROLE_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.springauthorizationexample.TestDatabaseContainer;
import com.example.springauthorizationexample.dto.LoginRequest;
import com.example.springauthorizationexample.dto.LoginResponse;
import com.example.springauthorizationexample.dto.RegisterUserRequest;
import com.example.springauthorizationexample.security.JwtUtil;
import com.example.springauthorizationexample.service.AuthService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class AuthControllerTest extends TestDatabaseContainer {

  @Autowired private JwtUtil jwtUtil;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private AuthService authService;

  private static final String URL = "/auth%s";

  @Test
  void testRegister() {

    var requestBody = new RegisterUserRequest("user_1", "password");
    var response =
        restTemplate.postForEntity(
            URL.formatted("/register"), new HttpEntity<>(requestBody), String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains(USER_REGISTERED_MESSAGE);
  }

  @Test
  void testLogin() {

    var requestBody = new LoginRequest("admin", "admin");
    var response =
        restTemplate.postForEntity(
            URL.formatted("/login"), new HttpEntity<>(requestBody), LoginResponse.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().token()).isNotBlank();
    assertThat(jwtUtil.extractUsername(response.getBody().token())).as("admin");
  }

  @Test
  void testUnauthorizedLogin() {
    var requestBody = new LoginRequest("random", "random");
    var response =
        restTemplate.postForEntity(
            URL.formatted("/login"), new HttpEntity<>(requestBody), LoginResponse.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testChangeRole() {
    var registerBody = new RegisterUserRequest("user_2", "password");
    var registerResponse = authService.registerUser(registerBody);

    assertThat(registerResponse.equals(USER_REGISTERED_MESSAGE)).isTrue();

    var loginRequest = new LoginRequest("admin", "admin");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("admin", jwtUtil.extractUsername(loginResponse.token()));

    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponse.token()));
    var requestEntity = new HttpEntity<>(headers);
    String url = String.format("/admin/change-role/%s?newRole=%s", "user_2", "ROLE_ADMIN");
    var response =
        restTemplate.exchange(URL.formatted(url), HttpMethod.PUT, requestEntity, String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains(USER_ROLE_UPDATED.formatted("ROLE_ADMIN"));
  }

  @Test
  void testChangeRoleThrowUserNotFound() {
    var loginRequest = new LoginRequest("admin", "admin");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("admin", jwtUtil.extractUsername(loginResponse.token()));

    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponse.token()));
    var requestEntity = new HttpEntity<>(headers);
    String url = String.format("/admin/change-role/%s?newRole=%s", "user_3", "ROLE_ADMIN");

    var request =
        restTemplate.exchange(URL.formatted(url), HttpMethod.PUT, requestEntity, String.class);
    assertThat(request.getStatusCode().equals(HttpStatus.BAD_REQUEST)).isTrue();
    assertEquals(USER_NOT_FOUND, request.getBody());
  }

  @Test
  void testChangeRoleThrowRoleNotFound() {
    var registerBody = new RegisterUserRequest("user_4", "password");
    var registerResponse = authService.registerUser(registerBody);

    assertThat(registerResponse.equals(USER_REGISTERED_MESSAGE)).isTrue();
    var loginRequest = new LoginRequest("admin", "admin");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("admin", jwtUtil.extractUsername(loginResponse.token()));

    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponse.token()));
    var requestEntity = new HttpEntity<>(headers);
    String url = String.format("/admin/change-role/%s?newRole=%s", "user_4", "ROLE_ROLE");

    var request =
        restTemplate.exchange(URL.formatted(url), HttpMethod.PUT, requestEntity, String.class);
    assertThat(request.getStatusCode().equals(HttpStatus.BAD_REQUEST)).isTrue();
    assertEquals(ROLE_NOT_FOUND, request.getBody());
  }
}
