package com.qunite.api.service;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.exception.InvalidPasswordException;
import com.qunite.api.exception.UserAlreadyExistsException;
import com.qunite.api.exception.UserNotFoundException;
import com.qunite.api.security.JwtService;
import com.qunite.api.security.TokenType;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import com.qunite.api.web.dto.user.UserUpdateDto;
import com.qunite.api.web.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final TokenService tokensService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserMapper userMapper;

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
  public User updateOne(String username, UserUpdateDto userData) {
    User userByCredentials = findByUsername(username).orElseThrow(() -> new UserNotFoundException(
        "User with login %s does not exist".formatted(username)));

    if (userData.getUsername() != null && isUsernameInUse(userData.getUsername())) {
      throw new UserAlreadyExistsException(
          "Username %s is already in use".formatted(userData.getUsername()));
    }
    if (userData.getEmail() != null && isEmailInUse(userData.getEmail())) {
      throw new UserAlreadyExistsException(
          "Email %s is already in use".formatted(userData.getEmail()));
    }

    if (userData.getUsername() != null &&
        !userByCredentials.getUsername().equals(userData.getUsername())) {
      userByCredentials.clearTokens();
    }

    return userRepository.save(userMapper.partialUpdate(userData, userByCredentials));
  }

  @Override
  @Transactional
  public Optional<User> findOne(Long userId) {
    return userRepository.findById(userId);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
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
  public AuthenticationResponse signIn(String login, String password) {
    var user = userRepository.findByEmailOrUsername(login)
        .orElseThrow(() -> new UserNotFoundException(
            "User with login %s does not exist".formatted(login)));
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new InvalidPasswordException("Invalid password");
    }
    return jwtService.createJwtTokens(user);
  }

  @Override
  @Transactional
  public AuthenticationResponse refreshTokens(String refreshToken) {
    DecodedJWT decodedJWT = jwtService.verifyToken(refreshToken, TokenType.REFRESH_TOKEN)
        .orElseThrow(() -> new JWTDecodeException("Invalid refresh token"));

    User user =
        findOne(Long.valueOf(decodedJWT.getSubject())).orElseThrow(() -> new UserNotFoundException(
            "User with id %s does not exist".formatted(decodedJWT.getSubject())));
    tokensService.deleteOne(refreshToken);

    return jwtService.createJwtTokens(user);
  }

  private boolean isUsernameInUse(String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  private boolean isEmailInUse(String email) {
    return userRepository.findByEmailOrUsername(email).isPresent();
  }
}