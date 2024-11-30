package com.example.springauthorizationexample.controller;

import com.example.springauthorizationexample.dto.CreateProjectRequest;
import com.example.springauthorizationexample.dto.ProjectResponse;
import com.example.springauthorizationexample.model.Project;
import com.example.springauthorizationexample.repository.ProjectRepository;
import com.example.springauthorizationexample.repository.UserRepository;
import com.example.springauthorizationexample.security.JwtUtil;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  public ProjectController(
      ProjectRepository projectRepository, UserRepository userRepository, JwtUtil jwtUtil) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
  }

  @GetMapping
  public ResponseEntity<List<ProjectResponse>> getProjects(
      @RequestHeader("Authorization") String token) {
    var username = jwtUtil.extractUsername(token);
    var logedUser =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    List<ProjectResponse> projects;
    if (logedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
      projects =
          projectRepository.findAll().stream()
              .map(
                  p ->
                      new ProjectResponse(
                          p.getId(), p.getName(), p.getDescription(), p.getOwner().getUsername()))
              .toList();
    } else {
      projects =
          projectRepository.findByOwnerUsername(username).stream()
              .map(
                  p ->
                      new ProjectResponse(
                          p.getId(), p.getName(), p.getDescription(), p.getOwner().getUsername()))
              .toList();
    }

    return ResponseEntity.ok(projects);
  }

  @PostMapping
  public ResponseEntity<String> createProject(
      @RequestBody CreateProjectRequest request, @RequestHeader("Authorization") String token) {
    var username = jwtUtil.extractUsername(token);

    var user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

    if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN users cannot create projects");
    }

    var project = new Project();
    project.setName(request.name());
    project.setDescription(request.description());
    project.setOwner(user);

    projectRepository.save(project);
    return ResponseEntity.ok("Project created successfully");
  }
}
