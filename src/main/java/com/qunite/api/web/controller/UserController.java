package com.qunite.api.web.controller;


import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.queue.QueueDto;
import com.qunite.api.web.dto.user.UserCreationDTO;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.dto.user.UserUpdateDto;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Controller")
@RestController
public class UserController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final QueueMapper queueMapper;

  @GetMapping
  @Operation(summary = "Get all users")
  public ResponseEntity<List<UserDto>> all() {
    return ResponseEntity.ok(userService.findAll().stream().map(userMapper::toDto).toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by id", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<UserDto> getById(@PathVariable Long id) {
    return ResponseEntity.of(userService.findOne(id).map(userMapper::toDto));
  }

  @GetMapping("/{id}/managed-queues")
  @Operation(summary = "Get queues where user is manager", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<List<QueueDto>> getManagedQueues(@PathVariable Long id) {
    return ResponseEntity.of(userService.getManagedQueues(id)
        .map(list -> list.stream()
            .map(queueMapper::toDto).toList()));
  }

  @GetMapping("/{id}/created-queues")
  @Operation(summary = "Get queues created by user", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<List<QueueDto>> getCreatedQueues(@PathVariable Long id) {
    return ResponseEntity.of(
        userService.getCreatedQueues(id)
            .map(list -> list.stream()
                .map(queueMapper::toDto).toList()));
  }

  @PostMapping//todo id
  @Operation(summary = "Create user")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
    var created = userService.createOne(userMapper.toEntity(userCreationDTO));
    return new ResponseEntity<>(userMapper.toDto(created), HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user by id")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    userService.deleteOne(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update user by id", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                            @Valid @RequestBody UserUpdateDto userUpdateDto) {
    return ResponseEntity.of(userService.findOne(id)
        .map(user -> userMapper.partialUpdate(userUpdateDto, user))
        .map(userService::createOne)
        .map(userMapper::toDto)
    );
  }
}
