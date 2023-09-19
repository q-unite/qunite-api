package com.qunite.api.service;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.TokenPair;
import com.qunite.api.domain.User;
import com.qunite.api.exception.InvalidPasswordException;
import com.qunite.api.exception.InvalidRefreshTokenException;
import com.qunite.api.exception.UserAlreadyExistsException;
import com.qunite.api.exception.UserNotFoundException;
import com.qunite.api.security.JwtService;
import com.qunite.api.security.TokenType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final TokenService tokensService;
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
  public User updateOne(User newUser) {
    boolean isUsernameInUse = isUsernameInUse(newUser);
    boolean isEmailInUse = isEmailInUse(newUser);

    if (isUsernameInUse) {
      throw new UserAlreadyExistsException(
          "Username %s is already in use".formatted(newUser.getUsername()));
    }
    if (isEmailInUse) {
      throw new UserAlreadyExistsException(
          "Email %s is already in use".formatted(newUser.getEmail()));
    }

    return userRepository.save(newUser);
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
    tokensService.invalidateUserTokens(userId);
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
  public TokenPair signIn(String login, String password) {
    var user = userRepository.findByEmailOrUsername(login)
        .orElseThrow(() -> new UserNotFoundException(
            "User with login %s does not exist".formatted(login)));
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new InvalidPasswordException("Invalid password");
    }

    return jwtService.createJwt(user);
  }

  @Override
  @Transactional
  public TokenPair refreshTokens(String refreshToken) {

    tokensService.findByValue(refreshToken).filter(tokenPair -> !tokenPair.isValid()).ifPresent(
        tokenPair -> {
          tokensService.invalidateUserTokens(tokenPair.getOwner().getId());
          throw new InvalidRefreshTokenException(
              "This refresh token has already been used");
        });

    DecodedJWT decodedJWT = jwtService.verifyJwt(refreshToken, TokenType.REFRESH)
        .orElseThrow(() -> new JWTDecodeException("Invalid refresh token"));

    User user = findOne(Long.valueOf(decodedJWT.getSubject()))
        .orElseThrow(() -> new UserNotFoundException(
            "User with id %s does not exist".formatted(decodedJWT.getSubject())));
    tokensService.invalidate(refreshToken);

    return jwtService.createJwt(user);
  }

  private boolean isUsernameInUse(User user) {
    return userRepository.findByUsername(user.getUsername())
        .filter(found -> !found.getId().equals(user.getId())).isPresent();
  }

  private boolean isEmailInUse(User user) {
    return userRepository.findByEmailOrUsername(user.getEmail())
        .filter(found -> !found.getId().equals(user.getId())).isPresent();
  }
}