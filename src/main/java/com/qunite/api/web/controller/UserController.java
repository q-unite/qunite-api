package com.qunite.api.web.controller;


import com.qunite.api.data.UserRepository;
import com.qunite.api.web.mapper.UserMapper;
import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: 03.05.2023 implement user controller 
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users", produces = MediaTypes.JSON_API_VALUE)
@RestController
public class UserController {
  private final UserRepository userRepository;
  private final UserMapper userMapper;


  @GetMapping("/{id}")
  public ResponseEntity<RepresentationModel<?>> getById(@PathVariable Long id) {
    return null;
  }

}
