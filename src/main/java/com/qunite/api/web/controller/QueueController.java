package com.qunite.api.web.controller;

import com.qunite.api.domain.Queue;
import com.qunite.api.service.QueueService;
import com.qunite.api.web.dto.entry.EntryDto;
import com.qunite.api.web.dto.queue.QueueCreationDto;
import com.qunite.api.web.dto.queue.QueueDto;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.mapper.EntryMapper;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
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
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<QueueDto> getById(@PathVariable Long id) {
    return ResponseEntity.of(queueService.findById(id).map(queueMapper::toDto));
  }

  @GetMapping("/{id}/members-amount")
  @Operation(summary = "Get members amount of queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<Integer> membersAmount(@PathVariable Long id) {
    return ResponseEntity.of(queueService.getMembersAmountInQueue(id));
  }

  @GetMapping("/{id}/members/{member-id}")
  @Operation(summary = "Get member's position in queue", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<Integer> memberPosition(@PathVariable(value = "id") Long queueId,
                                                @PathVariable(value = "member-id") Long memberId) {
    return ResponseEntity.of(queueService.getMemberPositionInQueue(memberId, queueId));
  }

  @GetMapping("/{id}/creator")
  @Operation(summary = "Get queue's creator", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<UserDto> getQueueCreator(@PathVariable Long id) {
    return ResponseEntity.of(
        queueService.findById(id).map(Queue::getCreator).map(userMapper::toDto)
    );
  }

  @GetMapping("/{id}/managers")
  @Operation(summary = "Get queue's managers", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<List<UserDto>> getQueueManagers(@PathVariable Long id) {
    return ResponseEntity.of(
        queueService.findById(id)
            .map(queue -> queue.getManagers().stream().map(userMapper::toDto).toList())
    );
  }

  @GetMapping("/{id}/entries")
  @Operation(summary = "Get queue's entries", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = {@Content()})
  })
  public ResponseEntity<List<EntryDto>> getQueueEntries(@PathVariable Long id) {
    return ResponseEntity.of(
        queueService.findById(id)
            .map(queue -> queue.getEntries().stream().map(entryMapper::toDto).toList())
    );
  }

  @PostMapping
  @Operation(summary = "Create queue")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<QueueDto> createQueue(
      @Valid @RequestBody QueueCreationDto queueCreationDto) {
    var created = queueService.create(queueMapper.toEntity(queueCreationDto));
    return new ResponseEntity<>(queueMapper.toDto(created), HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete queue by id")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    queueService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
