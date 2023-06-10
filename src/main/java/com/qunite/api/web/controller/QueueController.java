package com.qunite.api.web.controller;

import com.qunite.api.domain.Queue;
import com.qunite.api.service.QueueService;
import com.qunite.api.web.dto.EntryDto;
import com.qunite.api.web.dto.QueueDto;
import com.qunite.api.web.dto.UserDto;
import com.qunite.api.web.mapper.EntryMapper;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.UserMapper;
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
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/queues", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class QueueController {
  private final QueueMapper queueMapper;
  private final UserMapper userMapper;
  private final EntryMapper entryMapper;
  private final QueueService queueService;

  @GetMapping
  public ResponseEntity<List<QueueDto>> all() {
    return queueService.findAll().stream().map(queueMapper::toDto).collect(
        Collectors.collectingAndThen(Collectors.toList(), ResponseEntity::ok)
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<QueueDto> getById(@PathVariable Long id) {
    return queueService.findById(id).map(queueMapper::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/members-amount")
  public ResponseEntity<Integer> membersAmount(@PathVariable Long id) {
    return queueService.getMembersAmountInQueue(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/members/{member-id}")
  public ResponseEntity<Integer> memberPosition(@PathVariable(value = "id") Long queueId,
                                                @PathVariable(value = "member-id") Long memberId) {
    return queueService.getMemberPositionInQueue(memberId, queueId).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // TODO: 24.04.2023 replace RepresentationModel map() when UserConverter will be implemented
  @GetMapping("/{id}/creator")
  public ResponseEntity<UserDto> getQueueCreator(@PathVariable Long id) {
    return queueService.findById(id)
        .map(Queue::getCreator)
        .map(userMapper::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // TODO: 24.04.2023 replace RepresentationModel map() when UserConverter will be implemented
  @GetMapping("/{id}/managers")
  public ResponseEntity<List<UserDto>> getQueueManagers(@PathVariable Long id) {
    return queueService.findById(id)
        .map(queue -> queue.getManagers().stream().map(userMapper::toDto).toList())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // TODO: 24.04.2023 replace RepresentationModel map() when EntryConverter will be implemented
  @GetMapping("/{id}/entries")
  public ResponseEntity<List<EntryDto>> getQueueEntries(@PathVariable Long id) {
    return queueService.findById(id)
        .map(queue -> queue.getEntries().stream().map(entryMapper::toDto).toList())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/")
  public ResponseEntity<QueueDto> createQueue(@Valid @RequestBody QueueDto queueDto) {
    var created = queueService.create(queueMapper.toEntity(queueDto));
    return new ResponseEntity<>(queueMapper.toDto(created), HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    queueService.deleteById(id);
    return ResponseEntity.ok().build();
  }
}
