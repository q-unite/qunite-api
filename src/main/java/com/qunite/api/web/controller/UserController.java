package com.qunite.api.web.controller;


import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.ExceptionResponse;
import com.qunite.api.web.dto.queue.QueueDto;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.dto.user.UserUpdateDto;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@CrossOrigin(origins = {"${client.web.url}"})
@RequiredArgsConstructor
@RequestMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Controller")
@SecurityRequirement(name = "bearer_token")
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
      @ApiResponse(responseCode = "404", content = @Content)
  })
  public ResponseEntity<UserDto> getById(@PathVariable Long id) {
    return ResponseEntity.of(userService.findOne(id).map(userMapper::toDto));
  }

  @GetMapping("/{id}/managed-queues")
  @Operation(summary = "Get queues where user is manager", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content)
  })
  public ResponseEntity<List<QueueDto>> getManagedQueues(@PathVariable Long id) {
    return ResponseEntity.of(userService.getManagedQueues(id)
        .map(list -> list.stream()
            .map(queueMapper::toDto).toList()));
  }

  @GetMapping("/{id}/created-queues")
  @Operation(summary = "Get queues created by user", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content)
  })
  public ResponseEntity<List<QueueDto>> getCreatedQueues(@PathVariable Long id) {
    return ResponseEntity.of(
        userService.getCreatedQueues(id)
            .map(list -> list.stream()
                .map(queueMapper::toDto).toList()));
  }

  @GetMapping("/self")
  @Operation(summary = "Get authorized user", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content)
  })
  public ResponseEntity<UserDto> getSelf(Principal principal) {
    return ResponseEntity.of(userService.findByUsername(principal.getName())
        .map(userMapper::toDto));
  }

  @PatchMapping("/self")
  @Operation(summary = "Update authorized user", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "400", description = "Username or email are already in use",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
      @ApiResponse(responseCode = "404", content = @Content)
  })
  public ResponseEntity<UserDto> updateSelf(Principal principal,
                                            @Valid @RequestBody UserUpdateDto userUpdateDto) {
    return ResponseEntity.of(userService.findByUsername(principal.getName())
        .map(found -> userMapper.partialUpdate(userUpdateDto, found))
        .map(found -> userService.updateOne(principal.getName(), found))
        .map(userMapper::toDto));
  }

  @DeleteMapping("/self")
  @Operation(summary = "Delete authorized user", responses = {
      @ApiResponse(responseCode = "204"),
      @ApiResponse(responseCode = "404", content = @Content)
  })
  public ResponseEntity<Void> deleteSelf(Principal principal) {
    return userService.findByUsername(principal.getName())
        .map(user -> {
          userService.deleteOne(user.getId());
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }
}
