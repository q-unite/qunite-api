package com.qunite.api.web.controller;


import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.queue.QueueDto;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.dto.user.UserUpdateDto;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<UserDto> getById(Principal principal, @PathVariable Long id) {
    log.info("hi there");
    return userService.compareUserIdToLoginData(principal.getName(), id)
        .map(user -> ResponseEntity.of(userService.findOne(id).map(userMapper::toDto)))
        .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @GetMapping("/{id}/managed-queues")
  @Operation(summary = "Get queues where user is manager", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<List<QueueDto>> getManagedQueues(Principal principal,
                                                         @PathVariable Long id) {
    return userService.compareUserIdToLoginData(principal.getName(), id)
        .map(user -> ResponseEntity.of(userService.getManagedQueues(id)
            .map(list -> list.stream()
                .map(queueMapper::toDto).toList())))
        .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @GetMapping("/{id}/created-queues")
  @Operation(summary = "Get queues created by user", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<List<QueueDto>> getCreatedQueues(Principal principal,
                                                         @PathVariable Long id) {
    return userService.compareUserIdToLoginData(principal.getName(), id)
        .map(user -> ResponseEntity.of(
            userService.getCreatedQueues(id)
                .map(list -> list.stream()
                    .map(queueMapper::toDto).toList())))
        .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user by id", responses = @ApiResponse(responseCode = "204"))
  public ResponseEntity<Void> deleteById(Principal principal, @PathVariable Long id) {
    return userService.compareUserIdToLoginData(principal.getName(), id)
        .map(user -> {
          userService.deleteOne(id);
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update user by id", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<UserDto> updateUser(Principal principal, @PathVariable Long id,
                                            @Valid @RequestBody UserUpdateDto userUpdateDto) {
    return userService.compareUserIdToLoginData(principal.getName(), id)
        .map(user -> ResponseEntity.of(userService.findOne(id)
            .map(founded -> userMapper.partialUpdate(userUpdateDto, founded))
            .map(userService::createOne)
            .map(userMapper::toDto)))
        .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

  }
}
