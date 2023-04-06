package com.qunite.api.data;

import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EntityLifecycleTest {

    @Autowired
    private QueueRepository queueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntryRepository entryRepository;

    @Test
    @Sql(value = "/queue-create.sql")
    void deleteQueueDoesNotDeleteCreator() {
        queueRepository.deleteById(1L);

        assertFalse(queueRepository.existsById(1L));
        assertTrue(userRepository.existsById(1L));
    }

    @Test
    @Sql(value = "/queue-create.sql")
    void deleteUserDeletesCreatedQueues() {
        userRepository.deleteById(1L);

        assertFalse(queueRepository.existsById(1L));
    }

    @Test
    @Sql(value = "/queue-create.sql")
    void deleteQueueDeletesCreatedQueueInCreator() {
        queueRepository.deleteById(1L);

        assertFalse(queueRepository.existsByCreatorId(1L));
    }

    @Test
    @Sql(value = "/creator-create.sql")
    void addCreatedQueueInCreatorAddsQueue() {
        var creator = findEntityById(1L, userRepository);
        createQueue(creator);

        assertTrue(queueRepository.existsById(1L));
    }

    @Test
    @Sql(value = "/queue-create.sql")
    void deleteCreatedQueueInCreatorDeletesQueue() {
        var queue = findEntityById(1L, queueRepository);
        var creator = findEntityById(1L, userRepository);
        creator.removeCreatedQueue(queue);

        assertFalse(queueRepository.existsById(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteUserDeletesEntries() {
        userRepository.deleteById(1L);

        assertFalse(entryRepository.existsById(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void addEntryInMemberAddsEntryInQueue() {
        var member = findEntityById(1L, userRepository);
        var entry = findEntityById(1L, entryRepository);
        member.addEntry(entry);

        assertTrue(entryRepository.existsByQueueId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteMemberDeletesEntriesInQueue() {
        userRepository.deleteById(1L);

        assertFalse(entryRepository.existsByMemberId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryDeletesEntryInQueueAndMember() {
        entryRepository.deleteById(1L);

        assertFalse(entryRepository.existsByMemberId(1L));
        assertFalse(entryRepository.existsByQueueId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryInMemberDeletesEntryInQueue() {
        var member = findEntityById(1L, userRepository);
        var entry = findEntityById(1L, entryRepository);
        member.removeEntry(entry);

        assertFalse(entryRepository.existsByQueueId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryInQueueDeletesEntryInMember() {
        var queue = findEntityById(1L, queueRepository);
        var entry = findEntityById(1L, entryRepository);
        queue.removeEntry(entry);

        assertFalse(entryRepository.existsByMemberId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryDoesNotDeleteQueueAndMember() {
        entryRepository.deleteById(1L);

        assertEquals(1, queueRepository.count());
        assertEquals(2, userRepository.count());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void addManagerInQueueAddsManagedQueueInManager() {
        var queue = findEntityById(1L, queueRepository);
        var manager = findEntityById(1L, userRepository);
        queue.addManager(manager);

        assertEquals(1, manager.getManagedQueues().size());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void addManagedQueueInManagerAddsManagerInQueue() {
        var queue = findEntityById(1L, queueRepository);
        var manager = findEntityById(1L, userRepository);
        manager.addManagedQueue(queue);

        assertEquals(1, queue.getManagers().size());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void deleteManagerInQueueDeletesManagedQueueInManager() {
        var queue = findEntityById(1L, queueRepository);
        var manager = findEntityById(1L, userRepository);

        queue.addManager(manager);
        queue.removeManager(manager);

        assertEquals(0, manager.getManagedQueues().size());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void deleteManagedQueueInManagedDeletesManagerInQueue() {
        var queue = findEntityById(1L, queueRepository);
        var manager = findEntityById(1L, userRepository);
        manager.removeManagedQueue(queue);

        assertEquals(0, queue.getManagers().size());
    }

    @Test
    @Sql("/entries-and-users-create.sql")
    void queueEntriesAreSortedByTimeAndId() {
        var entries = entryRepository.findEntriesIdByQueueId(1L);

        assertEquals(List.of(27L, 22L, 19L, 14L, 1L), entries);
    }

    private <T> T findEntityById(Long id, JpaRepository<T, Long> jpaRepository) {
        return jpaRepository.findById(id).orElseThrow(AssertionError::new);
    }

    public Queue createQueue(User creator) {
        var queue = new Queue();
        queue.setName("Test Queue");
        creator.addCreatedQueue(queue);
        return queueRepository.save(queue);
    }

}
