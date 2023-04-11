package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueueService {
  private final QueueRepository queueRepository;
  private final UserRepository userRepository;
  private final EntryRepository entryRepository;

  @Transactional
  public Queue create(Queue queue) {
    return queueRepository.save(queue);
  }

  @Transactional
  public Optional<Entry> enrollMemberToQueue(Long memberId, Long queueId) {
    return Optional.ofNullable(memberId)
        .flatMap(userRepository::findById)
        .flatMap(user -> Optional.ofNullable(queueId)
            .flatMap(queueRepository::findById)
            .map(queue -> new Entry(user, queue))
            .map(entry -> {
              entry.getQueue().addEntry(entry);
              log.info("Member with id [{}] was enrolled to queue with id [{}]",
                  entry.getMember().getId(),
                  queueId);
              return entry;
            }));
  }

  @Transactional
  public Optional<Integer> getMembersAmountInQueue(Long queueId) {
    return findById(queueId)
        .map(queue -> queue.getEntries().size());
  }

  @Transactional
  public Optional<Integer> getMemberPositionInQueue(Long memberId, Long queueId) {
    return entryRepository.findById(new EntryId(memberId, queueId))
        .map(entry -> entry.getEntryIndex() + 1);
  }

  @Transactional
  public void deleteById(Long queueId) {
    queueRepository.deleteById(queueId);
    log.info("Queue with id [{}] was deleted", queueId);
  }

  public Optional<Queue> findById(Long queueId) {
    return queueRepository.findById(queueId);
  }
}