package com.example.springauthorizationexample.service;

import com.example.springauthorizationexample.dto.CreateProjectRequest;
import com.example.springauthorizationexample.dto.ProjectResponse;
import java.util.List;

public interface ProjectService {

  List<ProjectResponse> getProjects(String token);

  String createProject(CreateProjectRequest request, String token);
}
