package com.qunite.api.service;

import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import com.qunite.api.web.dto.user.UserUpdateDto;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User createOne(User user);

  User updateOne(String username, UserUpdateDto userData);

  Optional<User> findOne(Long id);

  Optional<User> findByUsername(String username);

  void deleteOne(Long id);

  List<User> findAll();

  Optional<List<Queue>> getCreatedQueues(Long userId);

  Optional<List<Queue>> getManagedQueues(Long userId);

  AuthenticationResponse signIn(String login, String password);

  AuthenticationResponse refreshTokens(String refreshToken);
}
