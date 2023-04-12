package com.qunite.api.service;

import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public Optional<User> createUser(User user) {
    return Optional.of(userRepository.save(user));
  }

  @Override
  @Transactional
  public Optional<User> getUser(Long id) {
    return Optional.ofNullable(id).flatMap(userRepository::findById);
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    Optional.ofNullable(id).ifPresent(userRepository::deleteById);
  }

}
