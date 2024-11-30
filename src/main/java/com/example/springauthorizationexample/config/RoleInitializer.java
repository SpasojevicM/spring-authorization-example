package com.example.springauthorizationexample.config;

import com.example.springauthorizationexample.model.Role;
import com.example.springauthorizationexample.model.User;
import com.example.springauthorizationexample.repository.RoleRepository;
import com.example.springauthorizationexample.repository.UserRepository;
import java.util.Collections;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public RoleInitializer(
      RoleRepository roleRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (!roleRepository.existsByName("ROLE_ADMIN")) {
      roleRepository.save(new Role("ROLE_ADMIN"));
    }
    if (!roleRepository.existsByName("ROLE_USER")) {
      roleRepository.save(new Role("ROLE_USER"));
    }
    if (!userRepository.existsByUsername("admin")) {
      var role = roleRepository.findByName("ROLE_ADMIN").orElse(new Role());
      userRepository.save(
          new User("admin", passwordEncoder.encode("admin"), Collections.singleton(role)));
    }
  }
}
