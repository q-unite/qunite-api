package com.qunite.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.exception.ForbiddenAccessException;
import com.qunite.api.utils.JpaRepositoryUtils;
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
    var queue = queueService.create(new Queue(), "First");
    var result = queueService.findAll();

    assertEquals(1, result.size());
    assertEquals(queue, result.get(0));
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testEnrollingUserToQueue() {
    var position = queueService.enrollMember("Second", 1L);

    assertTrue(entryRepository.existsById(new EntryId(2L, 1L)));
    assertThat(position).hasValue(1);
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
    var amountOfExistingQueue = queueService.getMembersAmount(1L);
    var amountOfAbsentQueue = queueService.getMembersAmount(100L);

    assertThat(amountOfExistingQueue).hasValue(5);
    assertThat(amountOfAbsentQueue).isNotPresent();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testGettingMemberPositionInQueue() {
    var memberPosition = queueService.getMemberPosition(3L, 1L);

    assertThat(memberPosition).hasValue(5);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangingMemberPositionForward() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(7L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    queueService.changeMemberPosition(7L, queueId, 2, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList).isEqualTo(expectedEntryIdList);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangingMemberPositionForwardWithGap() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId),
        new EntryId(5L, queueId));

    queueService.changeMemberPosition(5L, queueId, 50, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangingMemberPositionBackward() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(3L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId));

    queueService.changeMemberPosition(3L, queueId, 2, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList).isEqualTo(expectedEntryIdList);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangingMemberPositionOnItself() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    queueService.changeMemberPosition(5L, queueId, 2, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList).isEqualTo(expectedEntryIdList);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testChangingMemberPositionConcurrent() throws InterruptedException {
    var queueId = 1L;
    var username = "First";
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.execute(() -> queueService.changeMemberPosition(7L, queueId, 4, username));
    executor.execute(() -> queueService.changeMemberPosition(3L, queueId, 0, username));
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    assertAll(
        () -> assertThat(actualEntryIdList).hasSameSizeAs(expectedEntryIdList),
        () -> assertThat(actualEntryIdList).doesNotContainNull(),
        () -> assertThat(actualEntryIdList).containsAll(expectedEntryIdList)
    );
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testDeletingFirstMemberByCreatorOrManager() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    queueService.deleteMemberByCreatorOrManager(7L, queueId, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testDeletingMiddleMemberByCreatorOrManager() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    queueService.deleteMemberByCreatorOrManager(5L, queueId, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testDeletingLastMemberByCreatorOrManager() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId));

    queueService.deleteMemberByCreatorOrManager(3L, queueId, "First");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testDeletingMemberByNotAdminOrManager() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    assertThatThrownBy(() -> queueService.deleteMemberByCreatorOrManager(3L, queueId, "Fifth"))
        .isInstanceOf(ForbiddenAccessException.class);

    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testDeletingMemberByCreatorOrManagerConcurrent() throws InterruptedException {
    var queueId = 1L;
    var username = "First";
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.execute(() -> queueService.deleteMemberByCreatorOrManager(7L, queueId, username));
    executor.execute(() -> queueService.deleteMemberByCreatorOrManager(3L, queueId, username));
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);

    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);
    var actualEntryIdListSize = actualEntryIdList.size();
    var firstMaybeDeletedEntryId = new EntryId(7L, queueId);
    var secondMaybeDeletedEntryId = new EntryId(3L, queueId);

    assertThat(actualEntryIdList).doesNotContainNull();
    if (actualEntryIdListSize == 4) {
      assertThat(actualEntryIdList,
          either(not(contains(firstMaybeDeletedEntryId)))
              .or(not(contains(secondMaybeDeletedEntryId))));
    } else if (actualEntryIdListSize == 3) {
      assertThat(actualEntryIdList,
          not(contains(firstMaybeDeletedEntryId, secondMaybeDeletedEntryId)));
    } else {
      fail("unexpected actualEntryIdList size");
    }
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testLeavingFirstMember() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    queueService.leaveByMember(queueId, "Seventh");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testLeavingMiddleMember() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(4L, queueId),
        new EntryId(3L, queueId));

    queueService.leaveByMember(queueId, "Fifth");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testLeavingLastMember() {
    var queueId = 1L;
    var expectedEntryIdList = List.of(
        new EntryId(7L, queueId),
        new EntryId(6L, queueId),
        new EntryId(5L, queueId),
        new EntryId(4L, queueId));

    queueService.leaveByMember(queueId, "Third");
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);

    assertThat(actualEntryIdList)
        .isEqualTo(expectedEntryIdList)
        .doesNotContainNull();
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  @Test
  void testLeavingMemberConcurrent() throws InterruptedException {
    var queueId = 1L;
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.execute(() -> queueService.leaveByMember(queueId, "Seventh"));
    executor.execute(() -> queueService.leaveByMember(queueId, "Fifth"));
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);

    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(queueId);
    var actualEntryIdListSize = actualEntryIdList.size();
    var firstMaybeDeletedEntryId = new EntryId(7L, queueId);
    var secondMaybeDeletedEntryId = new EntryId(5L, queueId);

    assertThat(actualEntryIdList).doesNotContainNull();
    if (actualEntryIdListSize == 4) {
      assertThat(actualEntryIdList,
          either(not(contains(firstMaybeDeletedEntryId)))
              .or(not(contains(secondMaybeDeletedEntryId))));
    } else if (actualEntryIdListSize == 3) {
      assertThat(actualEntryIdList,
          not(contains(firstMaybeDeletedEntryId, secondMaybeDeletedEntryId)));
    } else {
      fail("unexpected actualEntryIdList size");
    }
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testAddingManagerByCreator() {
    var queueId = 1L;
    var managerId = 3L;
    var queueCreatorUsername = "First";

    queueService.addManager(managerId, queueId, queueCreatorUsername);
    var managersList = queueService.getManagers(queueId).orElseThrow();
    var manager = JpaRepositoryUtils.getById(managerId, userRepository);

    assertThat(managersList).containsOnly(manager);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  @Test
  void testDeletingManagerByCreator() {
    var queueId = 1L;
    var managerId = 3L;
    var queueCreatorUsername = "First";

    queueService.deleteManager(managerId, queueId, queueCreatorUsername);
    var managersList = queueService.getManagers(queueId).orElseThrow();
    var manager = JpaRepositoryUtils.getById(managerId, userRepository);

    assertThat(managersList).doesNotContain(manager);
  }

  @Sql({"/users-create.sql", "/queues-create.sql"})
  @Test
  void testAddingManagerByNotCreator() {
    var queueId = 1L;
    var managerId = 3L;
    var notCreatorUsername = "Fifth";

    assertThatThrownBy(() -> queueService.addManager(managerId, queueId, notCreatorUsername))
        .isInstanceOf(ForbiddenAccessException.class);
    var managersList = queueService.getManagers(queueId).orElseThrow();
    var manager = JpaRepositoryUtils.getById(managerId, userRepository);

    assertThat(managersList).doesNotContain(manager);
  }

  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  @Test
  void testDeletingManagerByNotCreator() {
    var queueId = 1L;
    var managerId = 3L;
    var notCreatorUsername = "Fifth";

    assertThatThrownBy(() -> queueService.deleteManager(managerId, queueId, notCreatorUsername))
        .isInstanceOf(ForbiddenAccessException.class);
    var managersList = queueService.getManagers(queueId).orElseThrow();
    var manager = JpaRepositoryUtils.getById(managerId, userRepository);

    assertThat(managersList).contains(manager);
  }
}