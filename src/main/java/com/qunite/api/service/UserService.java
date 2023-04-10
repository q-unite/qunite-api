package com.qunite.api.service;

import com.qunite.api.domain.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  void createUser(User user);

  Optional<User> getUser(Long id);

  void deleteUser(Long id);

}
