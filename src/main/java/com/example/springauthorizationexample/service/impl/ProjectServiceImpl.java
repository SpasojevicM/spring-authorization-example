package com.example.springauthorizationexample.service.impl;

import com.example.springauthorizationexample.dto.CreateProjectRequest;
import com.example.springauthorizationexample.dto.ProjectResponse;
import com.example.springauthorizationexample.model.Project;
import com.example.springauthorizationexample.repository.ProjectRepository;
import com.example.springauthorizationexample.repository.UserRepository;
import com.example.springauthorizationexample.security.JwtUtil;
import com.example.springauthorizationexample.service.ProjectService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.example.springauthorizationexample.service.impl.AuthServiceImpl.USER_NOT_FOUND;

@Service
public class ProjectServiceImpl implements ProjectService {

  public static final String CAN_NOT_CREATE_PROJECT = "ADMIN users cannot create projects";
  public static final String PROJECT_CREATED_SUCCESSFULLY = "Project created successfully";
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  public ProjectServiceImpl(
      ProjectRepository projectRepository, UserRepository userRepository, JwtUtil jwtUtil) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
  }

  @Override
  public List<ProjectResponse> getProjects(String token) {
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

    return projects;
  }

  @Override
  public String createProject(CreateProjectRequest request, String token) {
    var username = jwtUtil.extractUsername(token);

    var user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, CAN_NOT_CREATE_PROJECT);
    }

    var project = new Project();
    project.setName(request.name());
    project.setDescription(request.description());
    project.setOwner(user);

    projectRepository.save(project);
    return PROJECT_CREATED_SUCCESSFULLY;
  }
}
