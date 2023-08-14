package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import com.qunite.api.domain.Tokens;
import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User createOne(User user);

  User updateOne(String username, User newUser);

  Optional<User> findOne(Long id);

  Optional<User> findByUsername(String username);

  void deleteOne(Long id);

  List<User> findAll();

  Optional<List<Queue>> getCreatedQueues(Long userId);

  Optional<List<Queue>> getManagedQueues(Long userId);

  Tokens signIn(String login, String password);

  Tokens refreshTokens(String refreshToken);
}
