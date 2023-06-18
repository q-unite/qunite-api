package com.qunite.api.service;

import com.qunite.api.domain.Entry;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.service.QueueService;
import com.qunite.api.service.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthorizationUtils {
  private final UserService userService;
  private final QueueService queueService;

  public boolean authorizeUsersById(String username, Long userId) {
    Optional<User> requiredUserOptional = userService.findOne(userId);
    Optional<User> givenUserOptional = userService.findByUsernameOrEmail(username);

    if (requiredUserOptional.isEmpty() || givenUserOptional.isEmpty()) {
      return false;
    }

    return givenUserOptional.get().getId().equals(userId);
  }

  public boolean authorizeQueuesByCreator(String username, Long queueId) {
    Optional<User> requiredUserOptional = getQueueCreator(queueId);
    Optional<User> givenUserOptional = userService.findByUsernameOrEmail(username);

    if (requiredUserOptional.isEmpty() || givenUserOptional.isEmpty()) {
      return false;
    }

    return requiredUserOptional.get().getId().equals(givenUserOptional.get().getId());

  }


  public boolean authorizeQueuesByManager(String username, Long queueId) {
    Optional<Set<User>> managersOptional = getQueueManagers(queueId);
    Optional<User> givenUserOptional = userService.findByUsernameOrEmail(username);

    if (managersOptional.isEmpty() || givenUserOptional.isEmpty()) {
      return false;
    }

    return managersOptional.get().contains(givenUserOptional.get());

  }
  public boolean authorizeQueuesByMember(String username, Long queueId){
    Set<User> membersOptional = getQueueMembers(queueId);
    Optional<User> givenUserOptional = userService.findByUsernameOrEmail(username);

    if (membersOptional.isEmpty() || givenUserOptional.isEmpty()) {
      return false;
    }

    return membersOptional.contains(givenUserOptional.get());
  }

  private Optional<User> getQueueCreator(Long queueId) {
    return queueService.findById(queueId).map(Queue::getCreator);
  }

  private Optional<Set<User>> getQueueManagers(Long queueId) {
    return queueService.findById(queueId).map(Queue::getManagers);
  }

  private Set<User> getQueueMembers(Long queueId){
    Optional<Queue> queue =queueService.findById(queueId);
    return queue.map(Queue::getEntries).stream().flatMap(List::stream)
        .map(Entry::getMember)
        .collect(Collectors.toSet());
  }
}