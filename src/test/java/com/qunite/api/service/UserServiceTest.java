package com.qunite.api.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import com.qunite.api.generic.PostgreSQLFixture;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;


@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest implements PostgreSQLFixture {
  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;


  @AfterEach
  void clear() {
    userRepository.deleteAll();
  }

  @Test
  @Sql(value = "/users-create.sql")
  void getUserReturnsActualUser() {
    assertEquals(userService.getUser(1L), userRepository.findById(1L));
  }

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
