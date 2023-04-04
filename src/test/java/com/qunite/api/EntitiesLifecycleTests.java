package com.qunite.api;

import com.qunite.api.domain.Entry;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.repository.EntryRepository;
import com.qunite.api.repository.QueueRepository;
import com.qunite.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EntitiesLifecycleTests {

    @Autowired
    private QueueRepository queueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntryRepository entryRepository;

    @Test
    public void deleteUserDeletesCreatedQueues() {
        User creator = createUser("Test", "Creator");
        createQueue("Test Queue", creator);

        userRepository.delete(creator);

        assertEquals(0, queueRepository.count());
    }

    @Test
    public void deleteQueueDoesNotDeleteCreator() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);

        queueRepository.delete(queue);

        assertEquals(0, queueRepository.count());
        assertEquals(1, userRepository.count());
    }

    @Test
    public void deleteQueueDeletesCreatedQueueInCreator() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);

        queueRepository.delete(queue);
        long userCreatedQueuesCount = queueRepository.countByCreatorId(creator.getId());

        assertEquals(0, userCreatedQueuesCount);
    }

    @Test
    public void addCreatedQueueInCreatorAddsQueue() {
        User creator = createUser("Test", "Creator");

        Queue queue = createQueue("Test Queue", creator);

        assertEquals(1, queueRepository.count());
    }

    @Test
    public void deleteCreatedQueueInCreatorDeletesQueue() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);

        userRepository.flush();
        creator.removeCreatedQueue(queue);

        assertEquals(0, queueRepository.count());
    }

    @Test
    public void deleteUserDeletesEntries() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        userRepository.delete(member);

        assertEquals(0, entryRepository.count());
    }

    @Test
    public void addEntryInMemberAddsEntryInQueue() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        member.addEntry(entry);
        long queueEntriesCount = entryRepository.countByQueueId(queue.getId());

        assertEquals(1, queueEntriesCount);
    }

    @Test
    public void addEntryInQueueAddsEntryInMember() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        queue.addEntry(entry);
        long userEntriesCount = entryRepository.countByUserId(member.getId());

        assertEquals(1, userEntriesCount);
    }

    @Test
    public void deleteEntryInMemberDeletesEntryInQueue() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        userRepository.flush();
        member.removeEntry(entry);
        long queueEntriesCount = entryRepository.countByQueueId(queue.getId());

        assertEquals(0, queueEntriesCount);
    }

    @Test
    public void deleteEntryInQueueDeletesEntryInMember() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        queueRepository.flush();
        queue.removeEntry(entry);
        long userEntriesCount = entryRepository.countByUserId(member.getId());

        assertEquals(0, userEntriesCount);
    }

    @Test
    public void deleteMemberDeletesEntriesInQueue() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        userRepository.delete(member);
        long userEntriesCount = entryRepository.countByUserId(member.getId());

        assertEquals(0, userEntriesCount);
    }

    @Test
    public void deleteEntryDeletesEntryInQueueAndMember() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        entryRepository.delete(entry);
        long queueEntriesCount = entryRepository.countByQueueId(queue.getId());
        long userEntriesCount = entryRepository.countByUserId(member.getId());

        assertEquals(0, userEntriesCount);
        assertEquals(0, queueEntriesCount);
    }


    @Test
    public void deleteEntryDoesNotDeleteQueueAndMember() {
        User creator = createUser("Test", "Creator");
        User member = createUser("Test", "Member");
        Queue queue = createQueue("Test Queue", creator);
        Entry entry = createEntry(member, queue);

        entryRepository.delete(entry);

        assertEquals(1, queueRepository.count());
        assertEquals(2, userRepository.count());
    }

    @Test
    public void addManagerInQueueAddsManagedQueueInManager() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);
        User manager = createUser("Test", "Manager");

        queue.addManager(manager);
        manager = userRepository.findById(manager.getId())
                .orElseThrow(() -> new AssertionError());

        assertEquals(1, manager.getManagedQueues().size());
    }

    @Test
    public void addManagedQueueInManagerAddsManagerInQueue() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);
        User manager = createUser("Test", "Manager");

        manager.addManagedQueue(queue);
        queue = queueRepository.findById(queue.getId())
                .orElseThrow(() -> new AssertionError());

        assertEquals(1, queue.getManagers().size());
    }

    @Test
    public void deleteManagerInQueueDeletesManagedQueueInManager() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);
        User manager = createUser("Test", "Manager");
        queue.addManager(manager);

        queue.removeManager(manager);
        manager = userRepository.findById(manager.getId())
                .orElseThrow(() -> new AssertionError());

        assertEquals(0, manager.getManagedQueues().size());
    }

    @Test
    public void deleteManagedQueueInManagedDeletesManagerInQueue() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);
        User manager = createUser("Test", "Manager");
        manager.addManagedQueue(queue);

        manager.removeManagedQueue(queue);
        queue = queueRepository.findById(queue.getId())
                .orElseThrow(() -> new AssertionError());

        assertEquals(0, queue.getManagers().size());
    }

    @Test
    public void queueEntriesAreSortedByTimeAndId() {
        User creator = createUser("Test", "Creator");
        Queue queue = createQueue("Test Queue", creator);
        List<User> testUsers = fillWithUsers();
        List<Entry> testEntries = fillQueueWithEntries(queue, testUsers);

        List<Entry> entries = queueRepository.findById(queue.getId())
                .orElseThrow(() -> new AssertionError())
                .getEntries();

        assertEquals(testEntries, entries);
    }

    private User createUser(String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    private Queue createQueue(String name, User creator) {
        Queue queue = new Queue();
        queue.setName(name);
        creator.addCreatedQueue(queue);
        return queueRepository.save(queue);
    }

    private Entry createEntry(User user, Queue queue) {
        Entry entry = new Entry();
        user.addEntry(entry);
        queue.addEntry(entry);
        return entryRepository.save(entry);
    }

    private List<User> fillWithUsers() {
        return List.of(
                createUser("First", "Member"),
                createUser("Second", "Member"),
                createUser("Third", "Member"),
                createUser("Fourth", "Member"),
                createUser("Fifth", "Member"));
    }

    public List<Entry> fillQueueWithEntries(Queue queue, List<User> users) {
        return List.of(
                createEntry(users.get(0), queue),
                createEntry(users.get(1), queue),
                createEntry(users.get(2), queue),
                createEntry(users.get(3), queue),
                createEntry(users.get(4), queue));
    }


}
