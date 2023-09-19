package com.qunite.api.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.exception.UserAlreadyExistsException;
import com.qunite.api.utils.JpaRepositoryUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@IntegrationTest
class UserServiceTest {
  @Autowired
  private UserService userService;
  @Autowired
  private QueueService queueService;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private QueueRepository queueRepository;
  @Autowired
  private EntryRepository entryRepository;

  @AfterEach
  void cleanAll() {
    userRepository.deleteAll();
    queueRepository.deleteAll();
    entryRepository.deleteAll();
  }

  @Test
  @Sql("/users-create.sql")
  @DisplayName("Update should update user with valid data")
  void testUpdate() {
    var username = "NewUsername";

    var user = JpaRepositoryUtils.getById(1L, userRepository);
    user.setUsername(username);

    userService.updateOne(user);

    var actualUsername = JpaRepositoryUtils.getById(1L, userRepository).getUsername();
    assertThat(actualUsername).isEqualTo(username);
  }

  @Test
  @DisplayName("Update should not work with username or email in use")
  @Sql("/users-create.sql")
  void testUpdatingWithUsedLogin() {
    var username = "Second";
    var user = JpaRepositoryUtils.getById(1L, userRepository);

    user.setUsername(username);
    user.setEmail("User2@user.com");

    assertThatThrownBy(() ->
        userService.updateOne(user))
        .isInstanceOf(UserAlreadyExistsException.class)
        .message().contains(username);
  }

  @Test
  @Sql("/users-create.sql")
  void testGettingByExistingIdShouldReturnUser() {
    var user = userService.findOne(1L);
    assertThat(user).isPresent();
    assertEquals(user, userRepository.findById(1L));
  }

  @Test
  @Sql("/users-create.sql")
  void testGettingByNotExistingIdShouldReturnUser() {
    assertThat(userService.findOne(100L)).isEmpty();
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void testGettingManagedQueuesReturnsManagedQueues() {
    var queues = userService.getManagedQueues(1L);
    List<Queue> expectedQueues = getQueues(1L, 3L);
    assertThat(queues).hasValue(expectedQueues);
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void testGettingCreatedQueuesCreatesQueue() {
    var queues = userService.getCreatedQueues(1L);
    List<Queue> expectedQueues = getQueues(1L, 4L);
    assertThat(queues).hasValue(expectedQueues);
  }

  @Test
  void testCreatingUserCreatesNewUserWithEncryptedPassword() {
    var password = "password";

    User user = new User();
    user.setUsername("Creator");
    user.setEmail("Email");
    user.setPassword(password);

    user = userService.createOne(user);
    var result = userService.findAll();

    assertEquals(1, result.size());
    assertEquals(user, result.get(0));
    assertTrue(passwordEncoder.matches(password, result.get(0).getPassword()));
  }

  @Test
  @Sql(value = "/users-create.sql")
  void testDeletingExistingUserDeletesUser() {
    userService.deleteOne(1L);

    assertFalse(userRepository.existsById(1L));
  }

  private List<Queue> getQueues(Long... ids) {
    return Arrays.stream(ids).map(queueService::findById).map(Optional::orElseThrow).toList();
  }

  ;
}
