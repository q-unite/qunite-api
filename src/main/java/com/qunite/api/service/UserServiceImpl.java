package com.qunite.api.service;

import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public User createOne(User user) {
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public Optional<User> findOne(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  @Transactional
  public void deleteOne(Long userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  @Transactional
  public Optional<List<Queue>> getCreatedQueues(Long userId) {
    return userRepository.findById(userId).map(User::getCreatedQueues).map(List::copyOf);
  }

  @Override
  @Transactional
  public Optional<List<Queue>> getManagedQueues(Long userId) {
    return userRepository.findById(userId).map(User::getManagedQueues).map(List::copyOf);
  }


  @Override
  public UserDetails loadUserByUsername(String loginData) {
    //todo
    User user = userRepository.findByUsernameOrEmail(loginData, loginData)
        .orElseThrow(IllegalArgumentException::new);
    String username;
    username = loginData.equals(user.getUsername()) ? user.getUsername() : user.getPassword();

    return org.springframework.security.core.userdetails.User.builder()
        .username(username)
        .password(user.getPassword())
        .build();
  }
}
