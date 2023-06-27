package com.qunite.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@IntegrationTest
class QueueServiceTest {
  @Autowired
  private QueueService queueService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private QueueRepository queueRepository;

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
    entryRepository.deleteAll();
    queueRepository.deleteAll();
  }

  @Sql("/users-create.sql")
  @Test
  void testQueueCreation() {
    var queue = queueService.create(new Queue());
    var result = queueService.findAll();

    assertEquals(1, result.size());
    assertEquals(queue, result.get(0));
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testEnrollingUserToQueue() {
    queueService.enrollMemberToQueue(2L, 1L);

    assertTrue(entryRepository.existsById(new EntryId(2L, 1L)));
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testDeletingQueue() {
    queueService.deleteById(1L, null);

    assertFalse(queueRepository.existsById(1L));
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testGettingMembersAmountInQueue() {
    var amountOfExistingQueue = queueService.getMembersAmountInQueue(1L);
    var amountOfAbsentQueue = queueService.getMembersAmountInQueue(100L);

    assertThat(amountOfExistingQueue).hasValue(5);
    assertThat(amountOfAbsentQueue).isNotPresent();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testGettingMemberPositionInQueue() {
    var memberPosition = queueService.getMemberPositionInQueue(3L, 1L);

    assertThat(memberPosition).hasValue(5);
  }
}