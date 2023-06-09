package com.qunite.api.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.exception.UserAlreadyExistsException;
import com.qunite.api.security.JwtService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  @Transactional
  public User createOne(User user) {
    if (!userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      return userRepository.save(user);
    } else {
      throw new UserAlreadyExistsException("User already exists");
    }
  }

  @Override
  @Transactional
  public Optional<User> findOne(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userRepository.findUserByUsername(username);
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
  @Transactional
  public Optional<DecodedJWT> signIn(String login, String password) {
    return jwtService.verifyAccessToken(
        jwtService.createJwtToken(
            userRepository.findByEmailOrUsername(login)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException(login))));
  }
}
