package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import java.util.Optional;

public interface QueueService {
  Queue create(Queue queue);

  void enrollMemberToQueue(Long memberId, Long queueId);

  Optional<Integer> getMembersAmountInQueue(Long queueId);

  Optional<Integer> getMemberPositionInQueue(Long memberId, Long queueId);

  void deleteById(Long queueId);
}
