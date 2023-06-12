package com.qunite.api.web.controller;


import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.QueueDto;
import com.qunite.api.web.dto.UserDto;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: 03.05.2023 implement user controller
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users", produces = MediaTypes.JSON_API_VALUE)
@RestController
public class UserController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final QueueMapper queueMapper;


  @GetMapping
  public ResponseEntity<List<UserDto>> all() {
    return ResponseEntity.ok(userService.findAll().stream().map(userMapper::toDto).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getById(@PathVariable Long id) {
    return ResponseEntity.of(userService.findOne(id).map(userMapper::toDto));
  }
  @GetMapping("/{id}/managed")
  public ResponseEntity<List<QueueDto>> getManagedQueues(@PathVariable Long id) {
    return ResponseEntity.of(userService.getManagedQueues(id)
        .map(list -> list.stream()
            .map(queueMapper::toDto).toList()));
  }
  @GetMapping("/{id}/created")
  public ResponseEntity<List<QueueDto>> getCreatedQueues(@PathVariable Long id){
    return ResponseEntity.of(
        userService.getCreatedQueues(id)
            .map(list -> list.stream()
                .map(queueMapper::toDto).toList()));
  }
  @PostMapping("/")
  public ResponseEntity<UserDto> createQueue(@Valid @RequestBody UserDto userDto) {
    var created = userService.createOne(userMapper.toEntity(userDto));
    return new ResponseEntity<>(userMapper.toDto(created), HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    userService.deleteOne(id);
    return ResponseEntity.ok().build();
  }
}
