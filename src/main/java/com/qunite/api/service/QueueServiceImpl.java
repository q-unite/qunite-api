package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.exception.EntryNotFoundException;
import com.qunite.api.exception.ForbiddenAccessException;
import com.qunite.api.exception.QueueNotFoundException;
import com.qunite.api.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class QueueServiceImpl implements QueueService {
  private final QueueRepository queueRepository;
  private final UserRepository userRepository;
  private final EntryRepository entryRepository;
  private final UserService userService;
  private final QueueServiceImpl self;

  @Override
  @Transactional
  public Queue create(Queue queue, String username) {
    queue.setCreator(userService.findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException(
            "Invalid creator with username: %s".formatted(username))));
    return queueRepository.save(queue);
  }

  @Override
  public Queue update(Queue queue, String username) {
    if (isUserQueueCreatorOrManagerByCredentials(queue.getId(), username)) {
      return queueRepository.save(queue);
    } else {
      throw new ForbiddenAccessException("User %s is not a creator or manager".formatted(username));
    }
  }

  @Override
  @Transactional
  public Optional<Integer> enrollMember(String username, Long queueId) {
    self.saveEntry(username, queueId);
    return self.getMemberPosition(username, queueId);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveEntry(String username, Long queueId) {
    var queue = findById(queueId).orElseThrow(
        () -> new QueueNotFoundException("Could not find queue by id %d".formatted(queueId)));
    var user = userRepository.findByUsername(username).orElseThrow(
        () -> new UserNotFoundException("Could not find user by username %s".formatted(username)));
    if (!entryRepository.existsById(new EntryId(user.getId(), queueId))) {
      queue.addEntry(new Entry(user, queue));
    }
  }

  @Override
  @Transactional
  public Optional<Integer> getMembersAmount(Long queueId) {
    return findById(queueId).map(queue -> queue.getEntries().size());
  }

  @Override
  @Transactional
  public Optional<Integer> getMemberPosition(String username, Long queueId) {
    return userRepository.findByUsername(username)
        .flatMap(user -> entryRepository.findById(new EntryId(user.getId(), queueId))
            .map(entry -> entry.getEntryIndex() + 1));
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void changeMemberPosition(Long memberId, Long queueId,
                                   Integer newIndex, String principalName) {
    if (!isUserQueueCreatorOrManagerByCredentials(queueId, principalName)) {
      throw new ForbiddenAccessException(
          "User %s is not a creator or manager".formatted(principalName));
    }
    var entry = entryRepository.findById(new EntryId(memberId, queueId))
        .orElseThrow(() -> new EntryNotFoundException(
            "Could not find entry by memberId %s and queueId %s".formatted(memberId, queueId)));

    int currentIndex = entry.getEntryIndex();
    int newIndexChecked = Math.min(newIndex, entry.getQueue().getEntries().size() - 1);
    if (currentIndex != newIndexChecked) {
      int startIndex = Math.min(currentIndex, newIndexChecked);
      int endIndex = Math.max(currentIndex, newIndexChecked);
      int increment = Integer.compare(currentIndex, newIndexChecked);
      entryRepository.updateEntryIndices(queueId, startIndex, endIndex, increment);
      entry.setEntryIndex(newIndexChecked);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void deleteMemberByCreatorOrManager(Long memberId, Long queueId, String principalName) {
    if (isUserQueueCreatorOrManagerByCredentials(queueId, principalName)) {
      deleteMember(memberId, queueId);
    } else {
      throw new ForbiddenAccessException(
          "User %s is not a creator or manager".formatted(principalName));
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void leaveByMember(Long queueId, String principalName) {
    userRepository.findByUsername(principalName)
        .ifPresent(user -> deleteMember(user.getId(), queueId));
  }

  private void deleteMember(Long memberId, Long queueId) {
    entryRepository.findById(new EntryId(memberId, queueId)).ifPresent(entry -> {
      entryRepository.deleteById(entry.getId());
      entryRepository.updateEntryIndices(queueId, entry.getEntryIndex() + 1,
          Integer.MAX_VALUE, -1);
    });
  }

  @Override
  @Transactional
  public void deleteById(Long queueId, String principalName) {
    if (isUserQueueCreatorByCredentials(queueId, principalName)) {
      if (queueRepository.existsById(queueId)) {
        queueRepository.deleteById(queueId);
      } else {
        throw new QueueNotFoundException(
            "Could not find queue by id %d".formatted(queueId));
      }
    } else {
      throw new ForbiddenAccessException("User %s is not a creator".formatted(principalName));
    }
  }

  @Override
  @Transactional
  public Optional<List<User>> getManagers(Long queueId) {
    return queueRepository.findById(queueId).map(Queue::getManagers).map(List::copyOf);
  }

  @Override
  @Transactional
  public void addManager(Long managerId, Long queueId, String principalName) {
    updateManagers(managerId, queueId, principalName, (queue, manager) -> {
      if (queue.getCreator().getId().equals(managerId)) {
        throw new ForbiddenAccessException("You do not need to specify yourself as a manager ;)");
      }
      queue.addManager(manager);
    });
  }

  @Override
  @Transactional
  public void deleteManager(Long managerId, Long queueId, String principalName) {
    updateManagers(managerId, queueId, principalName, Queue::removeManager);
  }

  private void updateManagers(Long managerId, Long queueId, String principalName,
                              BiConsumer<Queue, User> consumer) {
    var queue = findById(queueId).orElseThrow(
        () -> new QueueNotFoundException("Could not find queue by id: %d".formatted(queueId)));
    if (queue.getCreator().getUsername().equals(principalName)) {
      var manager = userService.findOne(managerId).orElseThrow(
          () -> new UserNotFoundException("No user exists by given id: %d".formatted(managerId))
      );
      consumer.accept(queue, manager);
    } else {
      throw new ForbiddenAccessException("You can not modify this queue");
    }
  }

  private boolean isUserQueueCreatorByCredentials(Long queueId, String username) {
    return findById(queueId)
        .map(queue -> userService.findByUsername(username)
            .map(user -> queue.getCreator().equals(user))
            .orElse(false))
        .orElse(false);
  }

  private boolean isUserQueueCreatorOrManagerByCredentials(Long queueId, String username) {
    return findById(queueId)
        .filter(queue -> StreamEx.of(queue.getManagers()).append(queue.getCreator())
            .findAny(user -> user.getUsername().equals(username))
            .isPresent())
        .isPresent();
  }

  @Override
  public List<Queue> findAll() {
    return queueRepository.findAll();
  }

  @Override
  public Optional<Queue> findById(Long queueId) {
    return queueRepository.findById(queueId);
  }
}