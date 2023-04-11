package com.qunite.api.service;

import com.qunite.api.domain.User;
import java.util.Optional;

public interface UserService {
  Optional<User> createUser(User user);

  Optional<User> getUser(Long id);

  void deleteUser(Long id);

}
