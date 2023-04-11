package com.qunite.api.data;

import static com.qunite.api.utils.JpaRepositoryUtils.findEntityById;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.generic.PostgreSQLFixture;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EntityLifecycleTest implements PostgreSQLFixture {

  @Autowired
  private QueueRepository queueRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private EntryRepository entryRepository;

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteQueueDoesNotDeleteCreator() {
    queueRepository.deleteById(1L);

    assertFalse(queueRepository.existsById(1L));
    assertTrue(userRepository.existsById(1L));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteUserDeletesCreatedQueues() {
    userRepository.deleteById(1L);

    assertFalse(queueRepository.existsById(1L));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteQueueDeletesCreatedQueueInCreator() {
    queueRepository.deleteById(1L);

    assertFalse(queueRepository.existsByCreatorId(1L));
  }

  @Test
  @Sql("/users-create.sql")
  void addCreatedQueueInCreatorAddsQueue() {
    var creator = findEntityById(1L, userRepository);
    creator.addCreatedQueue(new Queue());

    assertFalse(queueRepository.findAll().isEmpty());
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteCreatedQueueInCreatorDeletesQueue() {
    var queue = findEntityById(1L, queueRepository);
    var creator = findEntityById(1L, userRepository);
    creator.removeCreatedQueue(queue);

    assertFalse(queueRepository.existsById(1L));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void deleteUserDeletesEntries() {
    userRepository.deleteById(1L);

    assertFalse(entryRepository.existsById(new EntryId(1L, 1L)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void deleteMemberDeletesEntriesInQueue() {
    userRepository.deleteById(1L);

    assertFalse(entryRepository.existsByMemberId(1L));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void deleteEntryDeletesEntryInQueueAndMember() {
    entryRepository.deleteById(new EntryId(3L, 1L));

    assertFalse(entryRepository.existsByMemberId(3L));
    assertFalse(entryRepository.existsById(new EntryId(3L, 1L)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void deleteEntryInMemberDeletesEntryInQueue() {
    var member = findEntityById(3L, userRepository);
    var entry = findEntityById(new EntryId(3L, 1L), entryRepository);
    member.removeEntry(entry);

    assertFalse(entryRepository.existsById(new EntryId(3L, 1L)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void deleteEntryInQueueDeletesEntryInMember() {
    var queue = findEntityById(1L, queueRepository);
    var entry = findEntityById(new EntryId(3L, 1L), entryRepository);
    queue.removeEntry(entry);

    assertFalse(entryRepository.existsByMemberId(3L));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void deleteEntryDoesNotDeleteQueueAndMember() {
    entryRepository.deleteById(new EntryId(3L, 1L));

    assertEquals(3, queueRepository.count());
    assertEquals(7, userRepository.count());
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void addManagerInQueueAddsManagedQueueInManager() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(1L, userRepository);
    queue.addManager(manager);

    assertEquals(1, manager.getManagedQueues().size());
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void addManagedQueueInManagerAddsManagerInQueue() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(1L, userRepository);
    manager.addManagedQueue(queue);

    assertEquals(1, queue.getManagers().size());
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteManagerInQueueDeletesManagedQueueInManager() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(2L, userRepository);

    queue.addManager(manager);
    queue.removeManager(manager);

    assertEquals(0, manager.getManagedQueues().size());
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void addEntryToQueueUpdatesQueueEntries() {
    var queue = findEntityById(1L, queueRepository);
    var member = findEntityById(3L, userRepository);
    var entry = new Entry(member, queue);

    queue.addEntry(entry);

    assertEquals(1L, queue.getEntries().size());
    assertTrue(entryRepository.existsById(new EntryId(3L, 1L)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void addEntryToUserSetsMemberAndUpdatesUserEntries() {
    var queue = findEntityById(1L, queueRepository);
    var member = findEntityById(3L, userRepository);
    var entry = new Entry();
    entry.setQueue(queue);

    member.addEntry(entry);

    assertEquals(1L, member.getEntries().size());
    assertTrue(entryRepository.existsById(new EntryId(3L, 1L)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteManagedQueueInManagedDeletesManagerInQueue() {
    var queue = findEntityById(2L, queueRepository);
    var manager = findEntityById(2L, userRepository);
    manager.removeManagedQueue(queue);

    assertEquals(0, queue.getManagers().size());
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void queueEntriesAreSortedByTimeAndId() {
    var actualEntryIdList = entryRepository.findEntriesIdsByQueueId(1L);
    var expectedEntryIdList = new ArrayList<>(List.of(
        new EntryId(7L, 1L),
        new EntryId(6L, 1L),
        new EntryId(5L, 1L),
        new EntryId(4L, 1L),
        new EntryId(3L, 1L)
    ));
    assertEquals(expectedEntryIdList, actualEntryIdList);
  }
}