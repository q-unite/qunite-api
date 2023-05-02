package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class QueueServiceImpl implements QueueService {
  private final QueueRepository queueRepository;
  private final UserRepository userRepository;
  private final EntryRepository entryRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Queue create(Queue queue) {
    return queueRepository.save(queue);
  }

  @Override
  @Transactional
  public void enrollMemberToQueue(Long memberId, Long queueId) {
    Optional.ofNullable(memberId)
        .flatMap(userRepository::findById)
        .flatMap(user -> Optional.ofNullable(queueId)
            .flatMap(queueRepository::findById)
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
    Optional.ofNullable(queueId).ifPresent(queueRepository::deleteById);
  }

  @Override
  public List<Queue> findAll() {
    return queueRepository.findAll();
  }

  @Override
  public Optional<Queue> findById(Long queueId) {
    return Optional.ofNullable(queueId).flatMap(queueRepository::findById);
  }
}