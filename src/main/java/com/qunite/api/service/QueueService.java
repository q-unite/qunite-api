package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import java.util.List;
import java.util.Optional;

public interface QueueService {

  List<Queue> findAll();

  Optional<Queue> findById(Long id);

  Queue create(Queue queue);

  void enrollMember(String username, Long queueId);

  Optional<Integer> getMembersAmount(Long queueId);

  Optional<Integer> getMemberPosition(Long memberId, Long queueId);

  void changeMemberPosition(Long memberId, Long queueId,
                            Integer newIndex, String requesterUsername);

  void deleteMember(Long memberId, Long queueId, String requesterUsername);

  void deleteById(Long queueId, String requesterUsername);
}
