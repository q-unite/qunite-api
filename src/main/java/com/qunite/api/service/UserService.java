package com.qunite.api.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User createOne(User user);

  Optional<User> findOne(Long id);
  Optional<User> findByUsernameOrEmail(String loginData);

  void deleteOne(Long id);

  List<User> findAll();

  Optional<List<Queue>> getCreatedQueues(Long userId);

  Optional<List<Queue>> getManagedQueues(Long userId);

  Optional<User> compareUserIdToLoginData(String loginData, Long id);

  User register(String username, String email, String password);

  Optional<DecodedJWT> signIn(String loginData, String password);

}
