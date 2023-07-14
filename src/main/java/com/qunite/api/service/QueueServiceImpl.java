package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.exception.EntryNotFoundException;
import com.qunite.api.exception.QueueNotFoundException;
import com.qunite.api.exception.UserForbiddenException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
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
  public void enrollMember(String username, Long queueId) {
    var queue = findById(queueId).orElseThrow(
        () -> new QueueNotFoundException("Could not find queue by id %d".formatted(queueId)));
    userRepository.findByUsername(username)
        .map(user -> new Entry(user, queue))
        .ifPresent(entry -> entry.getQueue().addEntry(entry));
  }

  @Override
  @Transactional
  public Optional<Integer> getMembersAmount(Long queueId) {
    return findById(queueId).map(queue -> queue.getEntries().size());
  }

  @Override
  @Transactional
  public Optional<Integer> getMemberPosition(Long memberId, Long queueId) {
    return entryRepository.findById(new EntryId(memberId, queueId))
        .map(entry -> entry.getEntryIndex() + 1);
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void changeMemberPosition(Long memberId, Long queueId,
                                   Integer newIndex, String requesterUsername) {
    var entry = entryRepository.findById(new EntryId(memberId, queueId))
        .orElseThrow(() -> new EntryNotFoundException(
            "Could not find entry by memberId %s and queueId %s".formatted(memberId, queueId)));
    if (isUserQueueCreatorOrManagerByCredentials(queueId, requesterUsername)) {
      var currentIndex = entry.getEntryIndex();
      if (!currentIndex.equals(newIndex)) {
        int startIndex = Math.min(currentIndex, newIndex);
        int endIndex = Math.max(currentIndex, newIndex);
        int increment = currentIndex < newIndex ? -1 : 1;

        entryRepository.updateEntryIndices(queueId, startIndex, endIndex, increment);
        entry.setEntryIndex(newIndex);
      }
    } else {
      throw new UserForbiddenException("User is not a creator or manager");
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void deleteMember(Long memberId, Long queueId, String requesterUsername) {
    var entry = entryRepository.findById(new EntryId(memberId, queueId))
        .orElseThrow(() -> new EntryNotFoundException(
            "Could not find entry by memberId %s and queueId %s".formatted(memberId, queueId)));
    if (isUserQueueCreatorOrManagerByCredentials(queueId, requesterUsername)) {
      entryRepository.deleteById(entry.getId());
      entryRepository.updateEntryIndices(queueId, entry.getEntryIndex() + 1,
          Integer.MAX_VALUE, -1);
    } else {
      throw new UserForbiddenException("User is not a creator or manager");
    }
  }

  @Override
  @Transactional
  public void deleteById(Long queueId, String requesterUsername) {
    if (queueRepository.existsById(queueId)) {
      if (isUserQueueCreatorByCredentials(queueId, requesterUsername)) {
        queueRepository.deleteById(queueId);
      } else {
        throw new UserForbiddenException("User is not a creator");
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