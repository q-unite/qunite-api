package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;

public interface QueueService {

  List<Queue> findAll();

  Optional<Queue> findById(Long id);

  Queue create(Queue queue, String username);

  Queue update(Queue queue, String username);

  Optional<Integer> enrollMember(String username, Long queueId);

  Optional<Integer> getMembersAmount(Long queueId);

  Optional<Integer> getMemberPosition(String username, Long queueId);

  void changeMemberPosition(Long memberId, Long queueId,
                            Integer newIndex, String principalName);

  void deleteMemberByCreatorOrManager(Long memberId, Long queueId, String principalName);

  void leaveByMember(Long queueId, String principalName);

  void deleteById(Long queueId, String principalName);

  Optional<List<User>> getManagers(Long queueId);

  void addManager(Long managerId, Long queueId, String principalName);

  void deleteManager(Long managerId, Long queueId, String principalName);
}
