package com.example.springauthorizationexample.controller;

import com.example.springauthorizationexample.dto.CreateProjectRequest;
import com.example.springauthorizationexample.dto.ProjectResponse;
import com.example.springauthorizationexample.service.ProjectService;
import java.util.List;
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

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping
  public ResponseEntity<List<ProjectResponse>> getProjects(
      @RequestHeader("Authorization") String token) {
    return ResponseEntity.ok(projectService.getProjects(token));
  }

  @PostMapping
  public ResponseEntity<String> createProject(
      @RequestBody CreateProjectRequest request, @RequestHeader("Authorization") String token) {
    return ResponseEntity.ok(projectService.createProject(request, token));
  }
}
