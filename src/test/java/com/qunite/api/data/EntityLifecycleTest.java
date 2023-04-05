package com.qunite.api.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(0, queueRepository.count());
    }

    @Test
    @Sql(value = "/queue-create.sql")
    void deleteQueueDeletesCreatedQueueInCreator() {
        queueRepository.deleteById(1L);

        assertEquals(0, queueRepository.countByCreatorId(1L));
    }

    @Test
    @Sql(value = "/queue-create.sql")
    void addCreatedQueueInCreatorAddsQueue() {
        assertEquals(1, queueRepository.count());
    }

    @Test
    @Sql(value = "/queue-create.sql")
    void deleteCreatedQueueInCreatorDeletesQueue() {
        var queue = findEntityByIdOrElseThrowException(1L, queueRepository);
        var creator = findEntityByIdOrElseThrowException(1L, userRepository);
        creator.removeCreatedQueue(queue);

        assertEquals(0, queueRepository.count());
    }


    @Test
    @Sql(value = "/entry-create.sql")
    void deleteUserDeletesEntries() {
        userRepository.deleteById(1L);

        assertEquals(0, entryRepository.count());
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void addEntryInMemberAddsEntryInQueue() {
        var member = findEntityByIdOrElseThrowException(1L, userRepository);
        var entry = findEntityByIdOrElseThrowException(1L, entryRepository);
        member.addEntry(entry);

        assertEquals(1, entryRepository.countByQueueId(1L));
    }



    @Test
    @Sql(value = "/entry-create.sql")
    void deleteMemberDeletesEntriesInQueue() {
        userRepository.deleteById(1L);

        assertEquals(0, entryRepository.countByMemberId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryDeletesEntryInQueueAndMember() {
        entryRepository.deleteById(1L);

        assertEquals(0, entryRepository.countByMemberId(1L));
        assertEquals(0, entryRepository.countByQueueId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryInMemberDeletesEntryInQueue() {
        var member = findEntityByIdOrElseThrowException(1L, userRepository);
        var entry = findEntityByIdOrElseThrowException(1L, entryRepository);
        member.removeEntry(entry);

        assertEquals(0, entryRepository.countByQueueId(1L));
    }

    @Test
    @Sql(value = "/entry-create.sql")
    void deleteEntryInQueueDeletesEntryInMember() {
        var queue = findEntityByIdOrElseThrowException(1L, queueRepository);
        var entry = findEntityByIdOrElseThrowException(1L, entryRepository);
        queue.removeEntry(entry);

        assertEquals(0, entryRepository.countByMemberId(1L));
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
        var queue = findEntityByIdOrElseThrowException(1L, queueRepository);
        var manager = findEntityByIdOrElseThrowException(1L, userRepository);
        queue.addManager(manager);

        assertEquals(1, manager.getManagedQueues().size());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void addManagedQueueInManagerAddsManagerInQueue() {
        var queue = findEntityByIdOrElseThrowException(1L, queueRepository);
        var manager = findEntityByIdOrElseThrowException(1L, userRepository);
        manager.addManagedQueue(queue);

        assertEquals(1, queue.getManagers().size());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void deleteManagerInQueueDeletesManagedQueueInManager() {
        var queue = findEntityByIdOrElseThrowException(1L, queueRepository);
        var manager = findEntityByIdOrElseThrowException(1L, userRepository);

        queue.addManager(manager);
        queue.removeManager(manager);

        assertEquals(0, manager.getManagedQueues().size());
    }

    @Test
    @Sql(value = "/manager-create.sql")
    void deleteManagedQueueInManagedDeletesManagerInQueue() {
        var queue = findEntityByIdOrElseThrowException(1L, queueRepository);
        var manager = findEntityByIdOrElseThrowException(1L, userRepository);
        manager.removeManagedQueue(queue);

        assertEquals(0, queue.getManagers().size());
    }

    @Test
    @Sql("/entries-and-users-create.sql")
    void queueEntriesAreSortedByTimeAndId() {
        var entries = entryRepository.findEntriesIdByQueueId(1L);

        assertEquals(List.of(27L, 22L, 19L, 14L, 1L), entries);
    }

    private <T> T findEntityByIdOrElseThrowException(Long id, JpaRepository<T, Long> jpaRepository) {
        return jpaRepository.findById(id).orElseThrow(AssertionError::new);
    }


}
