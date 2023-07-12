package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.exception.EntryNotFoundException;
import com.qunite.api.exception.QueueNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class QueueServiceImpl implements QueueService {
  private final QueueRepository queueRepository;
  private final UserRepository userRepository;
  private final EntryRepository entryRepository;
  private final UserService userService;

  @Override
  @Transactional
  public Queue create(Queue queue) {
    return queueRepository.save(queue);
  }

  @Override
  @Transactional
  public void enrollMemberToQueue(String username, Long queueId) {
    var queue = findById(queueId).orElseThrow(
        () -> new QueueNotFoundException("Could not find queue by id %d".formatted(queueId)));
    userRepository.findByUsername(username)
        .map(user -> new Entry(user, queue))
        .ifPresent(entry -> entry.getQueue().addEntry(entry));
  }

  @Override
  @Transactional
  public Optional<Integer> getMembersAmountInQueue(Long queueId) {
    return findById(queueId).map(queue -> queue.getEntries().size());
  }

  @Override
  @Transactional
  public Optional<Integer> getMemberPositionInQueue(Long memberId, Long queueId) {
    return entryRepository.findById(new EntryId(memberId, queueId))
        .map(entry -> entry.getEntryIndex() + 1);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void changeMemberPositionInQueue(Long memberId, Long queueId,
                                          Integer newIndex, String username) {
    var entry = entryRepository.findById(new EntryId(memberId, queueId))
        .orElseThrow(() -> new EntryNotFoundException(
            "Could not find entry by memberId %s and queueId %s".formatted(memberId, queueId)));
    if (isUserQueueCreatorOrManagerByCredentials(queueId, username)) {
      var currentIndex = entry.getEntryIndex();
      if (!currentIndex.equals(newIndex)) {
        int startIndex = Math.min(currentIndex, newIndex);
        int endIndex = Math.max(currentIndex, newIndex);
        int increment = currentIndex < newIndex ? -1 : 1;

        entryRepository.updateEntryIndexes(queueId, startIndex, endIndex, increment);
        entry.setEntryIndex(newIndex);
      }
    } else {
      throw new AccessDeniedException("User is not a creator or manager");
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void deleteMemberFromQueue(Long memberId, Long queueId, String username) {
    var entry = entryRepository.findById(new EntryId(memberId, queueId))
        .orElseThrow(() -> new EntryNotFoundException(
            "Could not find entry by memberId %s and queueId %s".formatted(memberId, queueId)));
    if (isUserQueueCreatorOrManagerByCredentials(queueId, username)) {
      entryRepository.deleteById(entry.getId());
      entryRepository.decrementEntryIndexes(entry.getQueue().getId(), entry.getEntryIndex());
    } else {
      throw new AccessDeniedException("User is not a creator or manager");
    }
  }

  @Override
  @Transactional
  public void deleteById(Long queueId, String username) {
    if (queueRepository.existsById(queueId)) {
      if (isUserQueueCreatorByCredentials(queueId, username)) {
        queueRepository.deleteById(queueId);
      } else {
        throw new AccessDeniedException("User is not a creator");
      }
    } else {
      throw new QueueNotFoundException(
          "Could not find queue by id %d".formatted(queueId));
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