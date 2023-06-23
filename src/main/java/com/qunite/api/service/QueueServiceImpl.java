package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
  public void enrollMemberToQueue(Long memberId, Long queueId) {
    userRepository.findById(memberId)
        .flatMap(user ->
            queueRepository.findById(queueId)
                .map(queue -> new Entry(user, queue)))
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
  @Transactional
  public void deleteById(Long queueId) {
    queueRepository.deleteById(queueId);
  }

  @Override
  public List<Queue> findAll() {
    return queueRepository.findAll();
  }

  @Override
  public Optional<Queue> findById(Long queueId) {
    return queueRepository.findById(queueId);
  }

  @Override
  @Transactional
  public Optional<Queue> findByUserComparingToCreatorId(Long queueId, String loginData) {
    return this.findById(queueId)
        .flatMap(queue -> Optional.of(queue.getCreator())
            .map(User::getId)
            .map(creatorId -> userService.compareUserIdToLoginData(loginData, creatorId))
            .flatMap(user -> user.isPresent() ? Optional.of(queue) : Optional.empty()));
  }

  @Override
  @Transactional
  public Optional<Queue> findByUserComparingToQueueManagers(Long queueId, String loginData) {
    return this.findById(queueId)
        .flatMap(queue -> queue.getManagers().stream()
            .map(User::getId)
            .anyMatch(managerId -> userService.compareUserIdToLoginData(loginData, managerId)
                .isPresent()) ? Optional.of(queue) : Optional.empty());
  }

  @Override
  @Transactional
  public Optional<Queue> findByUserComparingBoth(Long queueId, String loginData) {
    return this.findById(queueId)
        .flatMap(queue -> this.findByUserComparingToCreatorId(queueId, loginData)
            .map(queue::equals)
            .flatMap(condition -> condition ? Optional.of(Boolean.TRUE)
                : this.findByUserComparingToQueueManagers(queueId, loginData)
                .map(queue::equals))
            .flatMap(finalCondition -> finalCondition ? Optional.of(queue) : Optional.empty()));
  }
}