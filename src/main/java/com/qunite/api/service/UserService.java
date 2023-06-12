package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User createOne(User user);

  Optional<User> findOne(Long id);

  void deleteOne(Long id);

  List<User> findAll();

  Optional<List<Queue>> getCreatedQueues(Long userid);

  Optional<List<Queue>> getManagedQueues(Long userid);
}
