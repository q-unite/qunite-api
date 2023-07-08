package com.qunite.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    queueService.deleteById(1L, "First");

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

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangeMemberPositionForward() {
    var expectedEntryIdList = List.of(
        new EntryId(6L, 1L),
        new EntryId(5L, 1L),
        new EntryId(7L, 1L),
        new EntryId(4L, 1L),
        new EntryId(3L, 1L));

    queueService.changeMemberPositionInQueue(7L, 1L, 2);
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(1L);

    assertEquals(expectedEntryIdList, actualEntryIdList);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangeMemberPositionBackward() {
    var expectedEntryIdList = List.of(
        new EntryId(7L, 1L),
        new EntryId(6L, 1L),
        new EntryId(3L, 1L),
        new EntryId(5L, 1L),
        new EntryId(4L, 1L));

    queueService.changeMemberPositionInQueue(3L, 1L, 2);
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(1L);

    assertEquals(expectedEntryIdList, actualEntryIdList);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangeMemberPositionOnItself() {
    var expectedEntryIdList = List.of(
        new EntryId(7L, 1L),
        new EntryId(6L, 1L),
        new EntryId(5L, 1L),
        new EntryId(4L, 1L),
        new EntryId(3L, 1L));

    queueService.changeMemberPositionInQueue(5L, 1L, 2);
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(1L);

    assertEquals(expectedEntryIdList, actualEntryIdList);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangeMemberPositionConcurrent() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.execute(() -> queueService.changeMemberPositionInQueue(7L, 1L, 4));
    executor.execute(() -> queueService.changeMemberPositionInQueue(3L, 1L, 0));
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(1L);
    var expectedEntryIdList = List.of(
        new EntryId(7L, 1L),
        new EntryId(6L, 1L),
        new EntryId(5L, 1L),
        new EntryId(4L, 1L),
        new EntryId(3L, 1L));

    assertAll(
        () -> assertEquals(expectedEntryIdList.size(), actualEntryIdList.size()),
        () -> assertFalse(actualEntryIdList.contains(null)),
        () -> assertTrue(actualEntryIdList.containsAll(expectedEntryIdList))
    );
  }
}