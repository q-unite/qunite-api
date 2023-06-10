package com.qunite.api.service;

import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User createUser(User user);

  Optional<User> getUser(Long id);

  void deleteUser(Long id);

  List<User> findAll();

  Optional<User> findById(Long id);

}
