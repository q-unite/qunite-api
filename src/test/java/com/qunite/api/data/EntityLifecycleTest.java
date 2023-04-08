package com.qunite.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = "/data.sql")
class EntityLifecycleTest {

  @Autowired
  private QueueRepository queueRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private EntryRepository entryRepository;

  @Test
  void deleteQueueDoesNotDeleteCreator() {
    queueRepository.deleteById(2L);

    assertFalse(queueRepository.existsById(2L));
    assertTrue(userRepository.existsById(1L));
  }

  @Test
  void deleteUserDeletesCreatedQueues() {
    userRepository.deleteById(1L);

    assertFalse(queueRepository.existsById(2L));
  }

  @Test
  void deleteQueueDeletesCreatedQueueInCreator() {
    queueRepository.deleteById(2L);

    assertFalse(queueRepository.existsByCreatorId(1L));
  }

  @Test
  void addCreatedQueueInCreatorAddsQueue() {
    var creator = findEntityById(1L, userRepository);
    creator.addCreatedQueue(new Queue());
    assertTrue(queueRepository.existsById(1L));
  }

  @Test
  void deleteCreatedQueueInCreatorDeletesQueue() {
    var queue = findEntityById(2L, queueRepository);
    var creator = findEntityById(1L, userRepository);
    creator.removeCreatedQueue(queue);

    assertFalse(queueRepository.existsById(2L));
  }

  @Test
  void deleteUserDeletesEntries() {
    userRepository.deleteById(1L);

    assertFalse(entryRepository.existsById(1L));
  }

  @Test
  void addEntryInMemberAddsEntryInQueue() {
    var member = findEntityById(1L, userRepository);
    var entry = findEntityById(1L, entryRepository);
    member.addEntry(entry);

    assertTrue(entryRepository.existsByQueueId(2L));
  }

  @Test
  void deleteMemberDeletesEntriesInQueue() {
    userRepository.deleteById(1L);

    assertFalse(entryRepository.existsByMemberId(1L));
  }

  @Test
  void deleteEntryDeletesEntryInQueueAndMember() {
    entryRepository.deleteById(1L);

    assertFalse(entryRepository.existsByMemberId(1L));
    assertFalse(entryRepository.existsByQueueId(1L));
  }

  @Test
  void deleteEntryInMemberDeletesEntryInQueue() {
    var member = findEntityById(1L, userRepository);
    var entry = findEntityById(1L, entryRepository);
    member.removeEntry(entry);

    assertFalse(entryRepository.existsByQueueId(1L));
  }

  @Test
  void deleteEntryInQueueDeletesEntryInMember() {
    var queue = findEntityById(2L, queueRepository);
    var entry = findEntityById(1L, entryRepository);
    queue.removeEntry(entry);

    assertFalse(entryRepository.existsByMemberId(1L));
  }

  @Test
  void deleteEntryDoesNotDeleteQueueAndMember() {
    entryRepository.deleteById(1L);

    assertTrue(queueRepository.existsById(2L));
    assertTrue(userRepository.existsById(1L));
  }

  @Test
  void addManagerInQueueAddsManagedQueueInManager() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(1L, userRepository);
    queue.addManager(manager);

    assertEquals(1, manager.getManagedQueues().size());
  }

  @Test
  void addManagedQueueInManagerAddsManagerInQueue() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(1L, userRepository);
    manager.addManagedQueue(queue);

    assertEquals(1, queue.getManagers().size());
  }

  @Test
  void deleteManagerInQueueDeletesManagedQueueInManager() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(1L, userRepository);

    queue.addManager(manager);
    queue.removeManager(manager);

    assertEquals(0, manager.getManagedQueues().size());
  }

  @Test
  void deleteManagedQueueInManagedDeletesManagerInQueue() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(1L, userRepository);
    manager.removeManagedQueue(queue);

    assertEquals(0, queue.getManagers().size());
  }

  @Test
  void queueEntriesAreSortedByTimeAndId() {
    var entries = entryRepository.findEntriesIdByQueueId(2L);

    assertEquals(List.of(27L, 22L, 19L, 14L, 1L), entries);
  }

  private <T> T findEntityById(Long id, JpaRepository<T, Long> jpaRepository) {
    return jpaRepository.findById(id).orElseThrow(AssertionError::new);
  }

}
