package com.qunite.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.generic.PostgreSQLFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
class QueueServiceImplTest implements PostgreSQLFixture {

  @Autowired
  private QueueServiceImpl queueServiceImpl;

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
    var createdQueue = queueServiceImpl.create(new Queue());

    assertThat(createdQueue).isNotNull();
    assertEquals(1L, createdQueue.getId());
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testEnrollingUserToQueue() {
    queueServiceImpl.enrollMemberToQueue(2L, 1L);

    assertTrue(entryRepository.existsById(new EntryId(2L, 1L)));
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testDeletingQueue() {
    queueServiceImpl.deleteById(1L);

    assertFalse(queueRepository.existsById(1L));
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testGettingMembersAmountInQueue() {
    var amountOfExistingQueue = queueServiceImpl.getMembersAmountInQueue(1L);
    var amountOfAbsentQueue = queueServiceImpl.getMembersAmountInQueue(4L);

    assertThat(amountOfExistingQueue).hasValue(5);
    assertThat(amountOfAbsentQueue).isNotPresent();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testGettingMemberPositionInQueue() {
    var memberPosition = queueServiceImpl.getMemberPositionInQueue(3L, 1L);

    assertThat(memberPosition).hasValue(5);
  }
}