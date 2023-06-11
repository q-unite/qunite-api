package com.qunite.api.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@IntegrationTest
class UserServiceTest {
  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private QueueRepository queueRepository;

  @AfterEach
  void cleanAll() {
    userRepository.deleteAll();
    entryRepository.deleteAll();
    queueRepository.deleteAll();
  }

  @Test
  @Sql(value = "/users-create.sql")
  void getUserReturnsActualUser() {
    assertEquals(userService.getUser(1L), userRepository.findById(1L));
  }

  // TODO: 06.11.2023 After completing the test fix task, change the configuration in boostrap.yml
  @Test
  void createUserCreatesNewUser() {
    User user = new User();
    user.setFirstName("Creator");
    user.setLastName("Creator");

    userService.createUser(user);

    assertTrue(userRepository.existsById(1L));
    assertThat(userRepository.findById(user.getId())).hasValue(user);
  }

  @Test
  @Sql(value = "/users-create.sql")
  void deleteUserDeletesUser() {
    userService.deleteUser(1L);

    assertFalse(userRepository.existsById(1L));
  }

}
