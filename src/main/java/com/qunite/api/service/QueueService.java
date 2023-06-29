package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import java.util.List;
import java.util.Optional;

public interface QueueService {

  List<Queue> findAll();

  Optional<Queue> findById(Long id);

  Queue create(Queue queue);

  void enrollMemberToQueue(Long memberId, Long queueId);

  Optional<Integer> getMembersAmountInQueue(Long queueId);

  Optional<Integer> getMemberPositionInQueue(Long memberId, Long queueId);

  void changeMemberPositionInQueue(Long memberId, Long queueId, Integer newIndex);

  void deleteMemberFromQueue(Long memberId, Long queueId);

  void deleteById(Long queueId, String username);
}
