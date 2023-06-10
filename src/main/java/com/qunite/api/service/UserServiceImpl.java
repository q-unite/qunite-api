package com.qunite.api.service;

import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import java.util.List;
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
  public User createUser(User user) {
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public Optional<User> getUser(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  public Optional<User> findById(Long userId) {
    return userRepository.findById(userId);
  }

}
