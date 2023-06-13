package com.qunite.api.service;

import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
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
  public Optional<List<Queue>> getCreatedQueues(Long userid) {
    return userRepository.findById(userid).map(User::getCreatedQueues).map(List::copyOf);
  }

  @Override
  @Transactional
  public Optional<List<Queue>> getManagedQueues(Long userid) {
    return userRepository.findById(userid).map(User::getManagedQueues).map(List::copyOf);
  }

  @Transactional
  @Override
  public Optional<User> updateOne(Long id, User newData) {
    return userRepository.findById(id).map(user -> {
      user.setFirstName(newData.getFirstName());
      user.setLastName(newData.getLastName());
      return user;
    });
  }

}
