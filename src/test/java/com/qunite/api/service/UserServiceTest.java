package com.qunite.api.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
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
  private QueueRepository queueRepository;
  @Autowired
  private QueueService queueService;

  @Autowired
  private EntryRepository entryRepository;

  @AfterEach
  void cleanAll() {
    userRepository.deleteAll();
    queueRepository.deleteAll();
    entryRepository.deleteAll();
  }

  @Test
  @Sql(value = "/users-create.sql")
  void testGettingByExistingIdShouldReturnUser() {
    assertEquals(userService.findOne(1L), userRepository.findById(1L));
  }

  // TODO: 06.11.2023 After completing the test fix task, change the configuration in boostrap.yml
  @Test
  @Sql(value = "/users-create.sql")
  void testGettingByNotExistingIdShouldReturnUser() {
    assertThat(userService.findOne(100L)).isEmpty();
  }

  @Test
  @Sql(value = {"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void testGettingManagedQueuesReturnsManagedQueues() {
    var queues = userService.getManagedQueues(1L);
    List<Queue> expectedQueues =
        List.of(queueService.findById(1L).get(), queueService.findById(3L).get());
    assertThat(queues).hasValue(expectedQueues);
  }

  @Test
  @Sql(value = {"/users-create.sql", "/queues-create.sql"})
  void testGettingCreatedQueuesCreatesQueue() {
    var queues = userService.getCreatedQueues(1L);
    List<Queue> expectedQueues =
        List.of(queueService.findById(1L).get(), queueService.findById(4L).get());
    assertThat(queues).hasValue(expectedQueues);
  }

  @Test
  void testCreatingUserCreatesNewUser() {
    User user = new User();
    user.setFirstName("Creator");
    user.setLastName("Creator");

    userService.createOne(user);

    assertTrue(userRepository.existsById(1L));
    assertThat(userRepository.findById(user.getId())).hasValue(user);
  }

  @Test
  @Sql(value = "/users-create.sql")
  void testDeletingExistingUserDeletesUser() {
    userService.deleteOne(1L);

    assertFalse(userRepository.existsById(1L));
  }


}
