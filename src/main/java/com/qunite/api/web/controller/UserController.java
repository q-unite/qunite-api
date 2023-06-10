package com.qunite.api.web.controller;


import com.qunite.api.data.UserRepository;
import com.qunite.api.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "users", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class UserController {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable Long id) {
    return null;
  }
}
