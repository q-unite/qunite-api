package com.qunite.api.web.controller;

import com.qunite.api.domain.Queue;
import com.qunite.api.service.QueueService;
import com.qunite.api.web.dto.ExceptionResponse;
import com.qunite.api.web.dto.entry.EntryDto;
import com.qunite.api.web.dto.entry.EntryUpdateDto;
import com.qunite.api.web.dto.queue.QueueCreationDto;
import com.qunite.api.web.dto.queue.QueueDto;
import com.qunite.api.web.dto.queue.QueueUpdateDto;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.mapper.EntryMapper;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(path = "/queues", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Queue Controller")
@RestController
public class QueueController {
  private final QueueMapper queueMapper;
  private final UserMapper userMapper;
  private final EntryMapper entryMapper;
  private final QueueService queueService;

  @GetMapping
  @Operation(summary = "Get all queues")
  public ResponseEntity<List<QueueDto>> all() {
    return queueService.findAll().stream().map(queueMapper::toDto).collect(
        Collectors.collectingAndThen(Collectors.toList(), ResponseEntity::ok)
    );
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get queue by id", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<QueueDto> getById(@PathVariable Long id) {
    return ResponseEntity.of(queueService.findById(id).map(queueMapper::toDto));
  }

  @GetMapping("/{id}/members-amount")
  @Operation(summary = "Get members amount of queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<Integer> membersAmount(@PathVariable Long id) {
    return ResponseEntity.of(queueService.getMembersAmount(id));
  }

  @GetMapping("/{id}/members/{member-id}")
  @Operation(summary = "Get member's position in queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<Integer> memberPosition(@PathVariable Long id,
                                                @PathVariable(value = "member-id") Long memberId) {
    return ResponseEntity.of(queueService.getMemberPosition(memberId, id));
  }

  @PostMapping("/{id}/members")
  @Operation(summary = "Enroll member to queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<Void> enrollMember(@PathVariable Long id,
                                           Principal principal) {
    queueService.enrollMember(principal.getName(), id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}/members/{member-id}")
  @Operation(summary = "Delete member from queue by id", responses = {
      @ApiResponse(responseCode = "204"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<Void> deleteMember(@PathVariable Long id,
                                           @PathVariable(value = "member-id") Long memberId,
                                           Principal principal) {
    queueService.deleteMember(memberId, id, principal.getName());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/members/{member-id}/entries")
  @Operation(summary = "Change member position in queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<Void> changeMemberPosition(@PathVariable Long id,
                                                   @PathVariable(value = "member-id") Long memberId,
                                                   @Valid @RequestBody EntryUpdateDto entryDto,
                                                   Principal principal) {
    queueService.changeMemberPosition(memberId, id, entryDto.getEntryIndex(), principal.getName());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}/creator")
  @Operation(summary = "Get queue's creator", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<UserDto> getCreator(@PathVariable Long id) {
    return ResponseEntity.of(queueService.findById(id)
        .map(Queue::getCreator)
        .map(userMapper::toDto));
  }

  @GetMapping("/{id}/managers")
  @Operation(summary = "Get queue's managers", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<List<UserDto>> getManagers(@PathVariable Long id) {
    return ResponseEntity.of(
        queueService.getManagers(id)
            .map(list -> list.stream()
                .map(userMapper::toDto).toList()));
  }

  @PostMapping("/{id}/managers/{manager-id}")
  @Operation(summary = "Add manager to queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
  })
  public ResponseEntity<Void> addManager(@PathVariable Long id,
                                         @PathVariable(value = "manager-id") Long managerId,
                                         Principal principal) {
    queueService.addManager(managerId, id, principal.getName());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}/managers/{manager-id}")
  @Operation(summary = "Delete queue's manager", responses = {
      @ApiResponse(responseCode = "204"),
      @ApiResponse(responseCode = "404",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
  })
  public ResponseEntity<Void> deleteManager(@PathVariable Long id,
                                            @PathVariable(value = "manager-id") Long managerId,
                                            Principal principal) {
    queueService.deleteManager(managerId, id, principal.getName());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/members")
  @Operation(summary = "Get queue's entries", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<List<EntryDto>> getEntries(@PathVariable Long id) {
    return ResponseEntity.of(queueService.findById(id)
        .map(found -> found.getEntries().stream()
            .map(entryMapper::toDto).toList()));
  }

  @PostMapping
  @Operation(summary = "Create queue", responses = @ApiResponse(responseCode = "201"))
  public ResponseEntity<QueueDto> createQueue(
      @Valid @RequestBody QueueCreationDto queueCreationDto, Principal principal) {
    var created = queueService.create(queueMapper.toEntity(queueCreationDto), principal.getName());
    return new ResponseEntity<>(queueMapper.toDto(created), HttpStatus.CREATED);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update queue by id", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())
  })
  public ResponseEntity<QueueDto> update(@PathVariable Long id,
                                         @Valid @RequestBody QueueUpdateDto queueUpdateDto,
                                         Principal principal) {
    return ResponseEntity.of(queueService.findById(id)
        .map(queue -> queueMapper.partialUpdate(queueUpdateDto, queue))
        .map(queue -> queueService.update(queue, principal.getName()))
        .map(queueMapper::toDto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete queue by id", responses = @ApiResponse(responseCode = "204"))
  public ResponseEntity<Void> deleteById(Principal principal, @PathVariable Long id) {
    queueService.deleteById(id, principal.getName());
    return ResponseEntity.noContent().build();
  }
}
