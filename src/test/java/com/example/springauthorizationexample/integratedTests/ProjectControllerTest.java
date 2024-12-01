package com.example.springauthorizationexample.integratedTests;

import static com.example.springauthorizationexample.controller.AuthController.USER_REGISTERED_MESSAGE;
import static com.example.springauthorizationexample.service.impl.ProjectServiceImpl.CAN_NOT_CREATE_PROJECT;
import static com.example.springauthorizationexample.service.impl.ProjectServiceImpl.PROJECT_CREATED_SUCCESSFULLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.springauthorizationexample.TestDatabaseContainer;
import com.example.springauthorizationexample.dto.CreateProjectRequest;
import com.example.springauthorizationexample.dto.LoginRequest;
import com.example.springauthorizationexample.dto.ProjectResponse;
import com.example.springauthorizationexample.dto.RegisterUserRequest;
import com.example.springauthorizationexample.security.JwtUtil;
import com.example.springauthorizationexample.service.AuthService;
import com.example.springauthorizationexample.service.ProjectService;
import java.util.Collections;
import java.util.List;

import org.apache.juli.logging.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class ProjectControllerTest extends TestDatabaseContainer {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private AuthService authService;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private ProjectService projectService;

  private static final String URL = "/projects%s";

  @Test
  void createProject() {
    var registerBody = new RegisterUserRequest("user_projects_1", "password");
    var registerResponse = authService.registerUser(registerBody);

    assertThat(registerResponse.equals(USER_REGISTERED_MESSAGE)).isTrue();

    var loginRequest = new LoginRequest("user_projects_1", "password");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("user_projects_1", jwtUtil.extractUsername(loginResponse.token()));

    var requestBody = new CreateProjectRequest("project_1", "Project 1 description");
    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponse.token()));
    var requestEntity = new HttpEntity<>(requestBody, headers);

    var response = restTemplate.postForEntity(URL.formatted(""), requestEntity, String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertEquals(PROJECT_CREATED_SUCCESSFULLY, response.getBody());
  }

  @Test
  void createProjectThrowAdminCanNotCreate() {
    var loginRequest = new LoginRequest("admin", "admin");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("admin", jwtUtil.extractUsername(loginResponse.token()));

    var requestBody = new CreateProjectRequest("project_1", "Project 1 description");
    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponse.token()));
    var requestEntity = new HttpEntity<>(requestBody, headers);

    var response = restTemplate.postForEntity(URL.formatted(""), requestEntity, String.class);

    assertThat(response.getStatusCode().equals(HttpStatus.FORBIDDEN)).isTrue();
    assertEquals(CAN_NOT_CREATE_PROJECT, response.getBody());
  }

  @Test
  void getAllProjectsUser() {
    var registerBody = new RegisterUserRequest("user_projects_2", "password");
    var registerResponse = authService.registerUser(registerBody);

    assertThat(registerResponse.equals(USER_REGISTERED_MESSAGE)).isTrue();

    var loginRequest = new LoginRequest("user_projects_2", "password");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("user_projects_2", jwtUtil.extractUsername(loginResponse.token()));

    var projectRequest = new CreateProjectRequest("project_2", "Project 2 description");
    var projectCreated = projectService.createProject(projectRequest, loginResponse.token());

    assertEquals(PROJECT_CREATED_SUCCESSFULLY, projectCreated);

    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponse.token()));
    var response =
        restTemplate.exchange(
            URL.formatted(""),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<ProjectResponse>>() {});

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertNotNull(response.getBody());

    var responseProject = response.getBody().stream().filter(p -> p.name().equals("project_2")).findFirst().orElse(null);
    assertNotNull(responseProject);
    assertEquals(registerBody.username(),responseProject.ownerUsername());
  }

  @Test
  void getAllProjectsAdmin() {
    var registerBody = new RegisterUserRequest("user_projects_3", "password");
    var registerResponse = authService.registerUser(registerBody);

    assertThat(registerResponse.equals(USER_REGISTERED_MESSAGE)).isTrue();

    var loginRequest = new LoginRequest("user_projects_3", "password");
    var loginResponse = authService.loginUser(loginRequest);

    assertNotNull(loginResponse.token());
    assertEquals("user_projects_3", jwtUtil.extractUsername(loginResponse.token()));

    var projectRequest = new CreateProjectRequest("project_3", "Project 3 description");
    var projectCreated = projectService.createProject(projectRequest, loginResponse.token());

    assertEquals(PROJECT_CREATED_SUCCESSFULLY, projectCreated);

    var loginRequestAdmin = new LoginRequest("admin","admin");
    var loginResponseAdmin = authService.loginUser(loginRequestAdmin);

    assertNotNull(loginResponseAdmin.token());
    assertEquals("admin", jwtUtil.extractUsername(loginResponseAdmin.token()));

    var headers = new HttpHeaders();
    headers.setAll(Collections.singletonMap("Authorization", loginResponseAdmin.token()));
    var response =
            restTemplate.exchange(
                    URL.formatted(""),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<ProjectResponse>>() {});

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertNotNull(response.getBody());

    var responseProject = response.getBody().stream().filter(p -> p.name().equals("project_3")).findFirst().orElse(null);
    assertNotNull(responseProject);
    assertEquals(registerBody.username(),responseProject.ownerUsername());
  }
}
