package com.qunite.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "USERS")
@ToString
@NoArgsConstructor
public class User {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Getter
    @Setter
    @Column(name = "first_name", nullable = false)
    String firstName;

    @Getter
    @Setter
    @Column(name = "last_name", nullable = false)
    String lastName;

    @Access(AccessType.FIELD)
    @ToString.Exclude
    @OrderBy("id asc")
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Queue> createdQueues = new ArrayList<>();

    @Access(AccessType.FIELD)
    @ToString.Exclude
    @OrderBy("id asc")
    @ManyToMany
    @JoinTable(name = "QUEUES_MANAGERS",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "queue_id"))
    List<Queue> managedQueues = new ArrayList<>();

    @Access(AccessType.FIELD)
    @ToString.Exclude
    @OrderBy("id asc")
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    List<Entry> entries = new ArrayList<>();

    public void addCreatedQueue(Queue queue) {
        createdQueues.add(queue);
        queue.setCreator(this);
    }

    public void removeCreatedQueue(Queue queue) {
        createdQueues.remove(queue);
        queue.setCreator(null);
    }

    public void addManagedQueue(Queue queue) {
        managedQueues.add(queue);
        queue.managers.add(this);
    }

    public void removeManagedQueue(Queue queue) {
        managedQueues.remove(queue);
        queue.managers.remove(this);
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
        entry.setUser(this);
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);
        entry.queue.entries.remove(entry);
        entry.setUser(null);
        entry.setQueue(null);
    }

    public List<Queue> getCreatedQueues() {
        return Collections.unmodifiableList(createdQueues);
    }

    public List<Queue> getManagedQueues() {
        return Collections.unmodifiableList(managedQueues);
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
